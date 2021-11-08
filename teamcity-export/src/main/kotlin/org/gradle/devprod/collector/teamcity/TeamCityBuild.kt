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

fun Build.toTeamCityBuild() = TeamCityBuild(
    id.stringId,
    branch.name,
    status?.name,
    revisions.first { it.vcsRootInstance.vcsRootId.stringId == "Gradle_Branches_GradlePersonalBranches" }.version,
    queuedDateTime.toOffsetDateTime(),
    startDateTime?.toOffsetDateTime(),
    finishDateTime?.toOffsetDateTime(),
    state.name,
    buildConfigurationId.stringId,
    statusText,
    composite == true
)
