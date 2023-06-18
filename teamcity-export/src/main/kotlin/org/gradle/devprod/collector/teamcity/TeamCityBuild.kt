package org.gradle.devprod.collector.teamcity

import org.jetbrains.teamcity.rest.Build
import java.net.URLEncoder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class TeamCityBuild(
    val id: String,
    val branch: String?,
    val status: String?,
    val gitCommitId: String,
    val queuedDateTime: OffsetDateTime,
    val dependencyFinishedDateTime: OffsetDateTime?,
    val startDateTime: OffsetDateTime?,
    val finishDateTime: OffsetDateTime?,
    val state: String,
    val buildConfigurationId: String,
    val statusText: String?,
    val composite: Boolean,
    val buildScanUrls: List<String> = emptyList(),
    val buildHostName: String?,
    val buildHostType: String?,
)

fun Build.toTeamCityBuild(): TeamCityBuild {
    val revision = revisions
        .firstOrNull {
            val vcsRootId = it.vcsRootInstance.vcsRootId.stringId
            vcsRootId.startsWith("GradleBuildToo") ||
                vcsRootId == "GradleRelease" ||
                vcsRootId == "GradleMaster"
        }
    // For builds which have been cancelled early, there won't be any VCS root. We'll ignore those.
    require(revision != null) {
        "No revision found for ${buildConfigurationId.stringId}, ${id.stringId}: VCS roots: ${
            revisions.joinToString(
                ", ",
            ) { it.vcsRootInstance.vcsRootId.stringId }
        }"
    }
    return TeamCityBuild(
        id = id.stringId,
        branch = branch.name,
        status = status?.name,
        gitCommitId = revision.version,
        queuedDateTime = queuedDateTime.toOffsetDateTime(),
        dependencyFinishedDateTime = null,
        startDateTime = startDateTime?.toOffsetDateTime(),
        finishDateTime = finishDateTime?.toOffsetDateTime(),
        state = state.name,
        buildConfigurationId = buildConfigurationId.stringId,
        statusText = statusText,
        composite = composite == true,
        buildHostName = agent?.name,
        buildHostType = typeOfAgents(agent?.name),
    )
}

private fun typeOfAgents(agentName: String?): String {
    return agentName?.let {
        when {
            it.contains("ec2") -> "EC2"
            it.contains("windows") -> "WIN"
            it.contains("dev") -> "LINUX"
            it.contains("mac") -> "MAC"
            else -> "NA"
        }
    } ?: return "EMPTY"
}

fun TeamCityResponse.BuildBean.toTeamCityBuild(
    buildScans: List<String>,
    dependenciesFinishedTime: OffsetDateTime? = null,
) = TeamCityBuild(
    id = id.toString(),
    branch = branchName,
    status = status,
    gitCommitId = revisions.revision.first().version,
    queuedDateTime = parseRFC822(queuedDate),
    dependencyFinishedDateTime = dependenciesFinishedTime ?: parseRFC822(startDate),
    startDateTime = parseRFC822(startDate),
    finishDateTime = parseRFC822(finishDate),
    state = state,
    buildConfigurationId = buildType.id,
    statusText = statusText,
    composite = isComposite,
    buildScanUrls = buildScans,
    buildHostName = agent?.name,
    buildHostType = typeOfAgents(agent?.name),
)

private val rfc822: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ").withZone(ZoneId.systemDefault())

fun formatRFC822(instant: Instant): String {
    return URLEncoder.encode(rfc822.format(instant), Charsets.UTF_8)
}

fun parseRFC822(datelike: String): OffsetDateTime {
    return ZonedDateTime.parse(datelike, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"))
        .toInstant()
        .atOffset(OffsetDateTime.now().offset)
}
