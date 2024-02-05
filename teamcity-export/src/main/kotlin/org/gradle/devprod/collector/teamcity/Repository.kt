package org.gradle.devprod.collector.teamcity

import java.time.Instant
import java.time.OffsetDateTime

interface Repository {
    fun getBuildScanTagsById(buildScanId: String): List<String>

    fun getBuildById(id: String): TeamCityBuild?

    fun storeBuild(build: TeamCityBuild)

    fun latestFinishedBuildTimestamp(projectId: String): Instant

    fun updateLatestFinishedBuildTimestamp(projectId: String, timestamp: Instant)
}
