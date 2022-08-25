package org.gradle.devprod.collector.teamcity

import org.jetbrains.teamcity.rest.Build
import java.time.OffsetDateTime

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
    val buildScanUrls: List<String> = emptyList()
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
            composite == true
        )
    }
}
