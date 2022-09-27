package org.gradle.devprod.collector.teamcity

import org.jetbrains.teamcity.rest.Build
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
    val startDateTime: OffsetDateTime?,
    val finishDateTime: OffsetDateTime?,
    val state: String,
    val buildConfigurationId: String,
    val statusText: String?,
    val composite: Boolean,
    val buildScanUrls: List<String> = emptyList(),
    val buildHostName: String?,
    val buildHostType: String?
)

fun Build.toTeamCityBuild(): TeamCityBuild? {
    val revision = revisions
        .firstOrNull {
            val vcsRootId = it.vcsRootInstance.vcsRootId.stringId
            vcsRootId.startsWith("GradleBuildToo") ||
                vcsRootId == "GradleRelease" ||
                vcsRootId == "GradleMaster"
        }
    // For builds which have been cancelled early, there won't be any VCS root. We'll ignore those.
    if (revision == null) {
        println("No revision found for ${buildConfigurationId.stringId}, ${id.stringId}: VCS roots: ${revisions.joinToString(", ") { it.vcsRootInstance.vcsRootId.stringId }}")
    }
    return revision?.let {
        TeamCityBuild(
            id.stringId,
            branch.name,
            status?.name,
            it.version,
            queuedDateTime.toOffsetDateTime(),
            startDateTime?.toOffsetDateTime(),
            finishDateTime?.toOffsetDateTime(),
            state.name,
            buildConfigurationId.stringId,
            statusText,
            composite == true,
            buildHostName = agent?.name,
            buildHostType = typeOfAgents(agent?.name)
        )
    }
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

fun TeamCityResponse.BuildBean.toTeamCityBuild(buildScans: List<String>) =
    TeamCityBuild(
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
        buildScanUrls = buildScans,
        buildHostName = agent.name,
        buildHostType = typeOfAgents(agent?.name)
    )

private val rfc822: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault())

fun formatRFC822(instant: Instant): String {
    return rfc822.format(instant) + "%2B0000"
}

fun parseRFC822(datelike: String): OffsetDateTime {
    return ZonedDateTime.parse(datelike, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssZ"))
        .toInstant()
        .atOffset(OffsetDateTime.now().offset)
}
