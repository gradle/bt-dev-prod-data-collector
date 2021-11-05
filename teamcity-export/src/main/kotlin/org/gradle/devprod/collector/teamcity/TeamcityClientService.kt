package org.gradle.devprod.collector.teamcity

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class TeamcityClientService(
    @Value("${'$'}{teamcity.api.token}")
    private val teamCityApiToken: String,
    private val objectMapper: ObjectMapper
) {
    private
    val teamCityInstance: TeamCityInstance = TeamCityInstanceFactory.guestAuth("https://builds.gradle.org")

    private
    val teamCityRestApiBuildsUrl = "https://builds.gradle.org/app/rest/builds"

    private
    val client: WebClient = WebClient.create(teamCityRestApiBuildsUrl)

    private
    val pipelines = listOf("Master", "Release")

    private
    fun buildConfigurationsFor(pipeline: String): List<String> = listOf(
        "Gradle_${pipeline}_Check_Stage_QuickFeedbackLinuxOnly_Trigger",
        "Gradle_${pipeline}_Check_Stage_QuickFeedback_Trigger",
        "Gradle_${pipeline}_Check_Stage_ReadyforMerge_Trigger",
        "Gradle_${pipeline}_Check_Stage_ReadyforNightly_Trigger",
        "Gradle_${pipeline}_Check_Stage_ReadyforRelease_Trigger"
    )

    fun loadTriggerBuilds(): Sequence<TeamCityBuild> =
        pipelines.flatMap { pipeline ->
            buildConfigurationsFor(pipeline)
        }.asSequence().flatMap { buildConfiguration ->
            teamCityInstance
                .builds()
                .fromConfiguration(BuildConfigurationId(buildConfiguration))
                .includeCanceled()
                .includeFailed()
                .withAllBranches()
                .since(Instant.now().minus(5, ChronoUnit.DAYS))
                .all()
                .map { it.toTeamCityBuild() }
        }

    // The rest client has no "affectProject(id:Gradle_Master_Check)" buildLocator
    fun loadFailedBuilds(since: Instant): Sequence<TeamCityBuild> =
        pipelines.asSequence().flatMap { pipeline ->
            // We have ~200 failed builds per day
            var nextPageUrl: String? = loadingFailedBuildsUrl(
                pipeline,
                since
            )
            var buildIterator: Iterator<TeamCityResponse.BuildBean> = emptyList<TeamCityResponse.BuildBean>().iterator()
            generateSequence {
                if (buildIterator.hasNext()) {
                    buildIterator.next().toTeamCityBuild()
                } else if (nextPageUrl == null) {
                    null
                } else {
                    val nextPage = loadFailedBuilds(nextPageUrl!!)
                    nextPageUrl = nextPage.nextHref
                    buildIterator = nextPage.build.iterator()
                    if (buildIterator.hasNext()) {
                        buildIterator.next().toTeamCityBuild()
                    } else {
                        null
                    }
                }
            }
        }

    private
    fun TeamCityResponse.BuildBean.toTeamCityBuild() = TeamCityBuild(
        id.toString(),
        branchName,
        status,
        revisions.revision.first().version,
        parseRFC822(queuedDate),
        parseRFC822(startDate),
        parseRFC822(finishDate),
        state,
        buildType.id,
        statusText,
        isComposite,
        loadBuildScans()
    )

    private
    fun loadingFailedBuildsUrl(
        pipeline: String,
        start: Instant,
        pageSize: Int = 100
    ): String {
        val locators = mapOf(
            "affectedProject" to "(id:Gradle_${pipeline}_Check)",
            "status" to "FAILURE",
            "branch" to "default:any",
            "composite" to "false",
            "sinceDate" to formatRFC822(start)
        ).entries.joinToString(",") { "${it.key}:${it.value}" }

        val fields = "nextHref,count,build(id,agent(name),buildType(id,name,projectName),failedToStart,revisions(revision(version)),branchName,status,statusText,state,queuedDate,startDate,finishDate,composite)"

        return "${teamCityRestApiBuildsUrl}/?locator=${locators}&fields=$fields&count=$pageSize"
    }

    private
    fun WebClient.RequestHeadersSpec<*>.bearerAuth(): WebClient.RequestHeadersSpec<*> = header("Authorization", "Bearer $teamCityApiToken")

    private
    fun TeamCityResponse.BuildBean.loadBuildScans(): List<String> {
        val response: Mono<String> = client.get()
            .uri("/id:$id/artifacts/content/.teamcity/build_scans/build_scans.txt")
            .accept(MediaType.TEXT_PLAIN)
            .bearerAuth()
            .retrieve()
            .bodyToMono<String>()
            .onErrorResume(WebClientResponseException::class.java) {
                if (it.statusCode.value() == 404) Mono.just("") else Mono.error(it)
            }

        return response.blockOptional().orElse("").lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    private
    fun loadFailedBuilds(nextPageUrl: String): TeamCityResponse = client.get()
        .uri(URI.create(nextPageUrl))
        .accept(MediaType.APPLICATION_JSON)
        .bearerAuth()
        .retrieve()
        .bodyToMono<String>()
        .block()
        .let { objectMapper.readValue(it, TeamCityResponse::class.java) }

    private val rfc822: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault())

    private
    fun formatRFC822(instant: Instant): String {
        return rfc822.format(instant) + "%2B0000"
    }

    private fun parseRFC822(datelike: String): OffsetDateTime {
        return ZonedDateTime.parse(datelike, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"))
            .toInstant().atOffset(OffsetDateTime.now().offset)
    }
}
