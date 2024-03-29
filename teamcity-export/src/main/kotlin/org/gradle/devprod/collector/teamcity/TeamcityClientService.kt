package org.gradle.devprod.collector.teamcity

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.BuildState
import org.jetbrains.teamcity.rest.BuildStatus
import org.jetbrains.teamcity.rest.ProjectId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class TeamcityClientService(
    @Value("${'$'}{teamcity.api.token}") private val teamCityApiToken: String,
    private val objectMapper: ObjectMapper,
    private val repository: Repository,
    private val meterRegistry: MeterRegistry,
) {
    private val teamCityInstance: TeamCityInstance = TeamCityInstanceFactory
        .tokenAuth("https://builds.gradle.org", teamCityApiToken)
        .withTimeout(5, TimeUnit.MINUTES)

    private val requestCounter = Counter.builder("network_request_outgoing_total")
        .description("Outgoing network request counters")

    private val lastScheduledTriggerSeconds = mutableMapOf<String, Double>()

    private val requestCountingExchangeFilterFunction = object : LoggingExchangeFilterFunction() {
        override fun logResponse(request: ClientRequest, response: ClientResponse) {
            requestCounter
                .tag("client", this@TeamcityClientService.javaClass.simpleName)
                .tag("host", request.url().toURL().host)
                .tag("method", request.method().toString())
                .tag("status", response.statusCode().value().toString())
                .register(meterRegistry)
                .increment()
        }
    }

    private val client: WebClient = WebClient.builder().filter(requestCountingExchangeFilterFunction).build()

    private fun getDependencyBuilds(buildId: String): List<TeamCityBuild> {
        return teamCityInstance.build(BuildId(buildId)).snapshotDependencies.map { build ->
            repository.getBuildById(build.id.stringId) ?: storeBuild(build.toTeamCityBuild())
        }
    }

    private fun storeBuild(build: TeamCityBuild): TeamCityBuild {
        val dependencies = getDependencyBuilds(build.id)
        val dependenciesFinishTime = dependencies
            .map { it.finishDateTime }
            .sortedByDescending { it }
            .firstOrNull() ?: build.startDateTime!!
        val buildScanUrls = loadBuildScans(build.id)
        val hasRetriedBuild = checkBuildScansHaveRetriedBuild(buildScanUrls)

        val copy = build.copy(
            dependencyFinishedDateTime = dependenciesFinishTime,
            buildScanUrls = loadBuildScans(build.id),
            hasRetriedBuild = hasRetriedBuild,
        )
        repository.storeBuild(copy)
        return copy
    }

    private fun storeBuild(build: TeamCityResponse.BuildBean) {
        val dependencies = getDependencyBuilds(build.id.toString())
        val dependenciesFinishTime = dependencies.map { it.finishDateTime }.sortedByDescending { it }.firstOrNull()
        val buildScanUrl = loadBuildScans(build.id)
        val hasRetriedBuild = checkBuildScansHaveRetriedBuild(buildScanUrl)
        repository.storeBuild(build.toTeamCityBuild(loadBuildScans(build.id), hasRetriedBuild, dependenciesFinishTime))
    }

    private fun updateTeamCityExportTriggerMetric(projectIdPrefix: String) {
        val instant = Instant.now().epochSecond.toDouble()
        if (!lastScheduledTriggerSeconds.containsKey(projectIdPrefix)) {
            val tags: Tags = Tags.of("project", projectIdPrefix)
            Gauge.builder(
                "teamcity_export_last_scheduled_trigger_seconds",
                lastScheduledTriggerSeconds,
            ) { m -> m[projectIdPrefix]!! }
                .tags(tags)
                .register(meterRegistry)
        }
        lastScheduledTriggerSeconds[projectIdPrefix] = instant
    }

    private fun loadAndStoreBuildsForBuildType(buildTypeId: String, start: Instant, end: Instant) {
        var nextPageUrl: String? =
            loadingBuildsUrl(buildTypeId, start, end, buildState = BuildState.FINISHED)
        while (nextPageUrl != null) {
            println("Loading builds from $nextPageUrl")
            val currentPage = loadBuilds(nextPageUrl)
            nextPageUrl = currentPage.nextHref

            val buildIterator: Iterator<TeamCityResponse.BuildBean> = currentPage.build.iterator()
            while (buildIterator.hasNext()) {
                val build = buildIterator.next()
                storeBuild(build)
            }
        }
    }

    private fun loadAndStoreBuildsBetween(projectId: String, start: Instant, end: Instant) {
        val project = teamCityInstance.project(ProjectId(projectId))
        project.childProjects.forEach {
            loadAndStoreBuildsBetween(it.id.stringId, start, end)
        }
        project.buildConfigurations.parallelStream().forEach {
            loadAndStoreBuildsForBuildType(it.id.stringId, start, end)
        }
    }

    fun loadAndStoreBuildsSinceLastCheckpoint(projectId: String) {
        val defaultWindowsSize = Duration.ofHours(1)
        var start = repository.latestFinishedBuildTimestamp(projectId)
        var end = start.plus(defaultWindowsSize)
        updateTeamCityExportTriggerMetric(projectId)
        while (end < Instant.now()) {
            loadAndStoreBuildsBetween(projectId, start, end)
            repository.updateLatestFinishedBuildTimestamp(projectId, end)
            start = end
            end = end.plus(defaultWindowsSize)
        }
    }

    /**
     * Get the builds whose finishedTime is between start and end
     * See https://www.jetbrains.com/help/teamcity/rest/get-build-details.html#Get+Specific+Builds
     */
    private fun loadingBuildsUrl(
        buildTypeId: String,
        start: Instant,
        end: Instant,
        buildStatus: BuildStatus? = null,
        buildState: BuildState? = null,
        composite: Boolean? = null,
        pageSize: Int = 100,
    ): String {
        val locators = mutableListOf(
            "buildType" to buildTypeId,
            "branch" to "default:any",
            "finishDate" to "(date:${formatRFC822(start)},condition:after)",
            "finishDate" to "(date:${formatRFC822(end)},condition:before)",
        )

        buildStatus?.let { locators.add("status" to it.toString().lowercase()) }
        buildState?.let { locators.add("state" to it.toString().lowercase()) }
        composite?.let { locators.add("composite" to it.toString()) }

        val locatorString = locators.joinToString(",") { "${it.first}:${it.second}" }

        val fields =
            "nextHref,count,build(id,agent(name),buildType(id,name,projectName),failedToStart,revisions(revision(version)),branchName,status,statusText,state,queuedDate,startDate,finishDate,composite)"

        return "/app/rest/builds/?locator=$locatorString&fields=$fields&count=$pageSize"
    }

    private fun WebClient.RequestHeadersSpec<*>.bearerAuth(): WebClient.RequestHeadersSpec<*> =
        header("Authorization", "Bearer $teamCityApiToken")

    private fun checkBuildScansHaveRetriedBuild(buildScanUrls: List<String>): Boolean {
        return buildScanUrls.map { it.substringAfter("/s/") }
            .map { repository.getBuildScanTagsById(it) }
            .any { tags -> tags.contains(RETRIED_BUILD_TAG) }
    }

    private fun loadBuildScans(id: Any): List<String> {
        val response: Mono<String> =
            client
                .get()
                .uri(createTeamcityUri("/app/rest/builds/id:$id/artifacts/content/.teamcity/build_scans/build_scans.txt"))
                .accept(MediaType.TEXT_PLAIN)
                .bearerAuth()
                .retrieve()
                .bodyToMono<String>()
                .onErrorResume(WebClientResponseException::class.java) {
                    if (it.statusCode.value() == 404) Mono.just("") else Mono.error(it)
                }

        return response.blockOptional().orElse("").lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun loadBuilds(nextPageUrl: String): TeamCityResponse =
        client
            .get()
            .uri(createTeamcityUri(nextPageUrl))
            .accept(MediaType.APPLICATION_JSON)
            .bearerAuth()
            .retrieve()
            .bodyToMono<String>()
            .block()
            .let { objectMapper.readValue(it, TeamCityResponse::class.java) }

    private fun createTeamcityUri(url: String): URI {
        return if (url.startsWith("http")) {
            URI.create(url)
        } else {
            val relativePath = if (url.startsWith("/")) url else "/$url"
            URI.create("https://builds.gradle.org$relativePath")
        }
    }
}
