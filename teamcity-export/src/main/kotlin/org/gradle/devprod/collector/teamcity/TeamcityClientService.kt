package org.gradle.devprod.collector.teamcity

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class TeamcityClientService(
    @Value("${'$'}{teamcity.api.token}")
    private val teamCityApiToken: String
) {
    private
    val teamCityInstance: TeamCityInstance = TeamCityInstanceFactory.guestAuth("https://builds.gradle.org")

    private
    val pipelines = listOf("Master", "Release")

    private
    val objectMapper = ObjectMapper()

    private
    val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()

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

    fun loadFailedBuilds(): Sequence<TeamCityBuild> =
        pipelines.asSequence().flatMap { pipeline ->
            var nextPageUrl: String? = initUrl(pipeline)
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
        loadBuildScans()
    )

    private
    fun initUrl(pipeline: String): String {
        // We have ~200 failed builds per day
        val pageSize = 100
        val end = Instant.now()
        // this is triggered once per hour, let's make the timespan a bit longer
        val start = end.minus(61, ChronoUnit.MINUTES)
        val locators = mapOf(
            "affectedProject" to "(id:Gradle_${pipeline}_Check)",
            "failedToStart" to "true",
            "status" to "FAILURE",
            "branch" to "default:any",
            "sinceDate" to formatRFC822(start),
            "untilDate" to formatRFC822(end),
        )
        val fields = "nextHref,count,build(id,agent(name),buildType(id,name,projectName),failedToStart,revisions(revision(version)),branchName,status,statusText,state,queuedDate,startDate,finishDate)"
        return "https://builds.gradle.org/app/rest/builds/?locator=${locators.entries.joinToString(",") { "${it.key}:${it.value}" }}&fields=$fields&count=$pageSize"
    }

    private
    fun TeamCityResponse.BuildBean.loadBuildScans(): List<String> {
        val response = invokeTeamCityApi("https://builds.gradle.org/app/rest/builds/id:$id/artifacts/content/.teamcity/build_scans/build_scans.txt", "text/plain")
        if (response.statusCode() == 404) {
            return emptyList()
        }
        require(response.statusCode() in 200..299) {
            "Get response ${response.statusCode()}: ${response.body()}"
        }
        return response.body().lines().map { it.trim() }.filter { it.isNotBlank() }
    }

    private
    fun invokeTeamCityApi(url: String, accept: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder(URI(url))
            .header("Accept", accept)
            .header("Authorization", "Bearer $teamCityApiToken")
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private
    fun loadFailedBuilds(nextPageUrl: String): TeamCityResponse {
        val response = invokeTeamCityApi(nextPageUrl, "application/json")
        require(response.statusCode() in 200..299) {
            "Get response ${response.statusCode()}: ${response.body()}"
        }
        return objectMapper.readValue(response.body(), TeamCityResponse::class.java)
    }

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
