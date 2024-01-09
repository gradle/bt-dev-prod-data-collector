package org.gradle.devprod.collector.teamcity

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.BuildState
import org.jetbrains.teamcity.rest.BuildStatus
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

    fun loadAndStoreAllBuilds(since: Instant, pipelineProjectIds: List<String>) {
        pipelineProjectIds.forEach { affectedProject ->
            var nextPageUrl: String? = loadingBuildsUrl(affectedProject, since, buildState = BuildState.FINISHED)
            while (nextPageUrl != null) {
                val currentPage = loadBuilds(nextPageUrl)
                nextPageUrl = currentPage.nextHref

                val buildIterator: Iterator<TeamCityResponse.BuildBean> = currentPage.build.iterator()
                while (buildIterator.hasNext()) {
                    val build = buildIterator.next()
                    storeBuild(build)
                }
            }
        }
    }

    private fun loadingBuildsUrl(
        affectedProject: String,
        start: Instant,
        buildStatus: BuildStatus? = null,
        buildState: BuildState? = null,
        composite: Boolean? = null,
        pageSize: Int = 100,
    ): String {
        val locators = mutableMapOf(
            "affectedProject" to "(id:$affectedProject)",
            "branch" to "default:any",
            "sinceDate" to formatRFC822(start),
        )

        buildStatus?.let { locators["status"] = it.toString().lowercase() }
        buildState?.let { locators["state"] = it.toString().lowercase() }
        composite?.let { locators["composite"] = it.toString() }

        val locatorString = locators.entries.joinToString(",") { "${it.key}:${it.value}" }

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
        val newUrl = url.replace("lookupLimit:10000", "lookupLimit:1000")
        return if (newUrl.startsWith("http")) {
            URI.create(newUrl)
        } else {
            val relativePath = if (newUrl.startsWith("/")) newUrl else "/$newUrl"
            URI.create("https://builds.gradle.org$relativePath")
        }
    }
}
