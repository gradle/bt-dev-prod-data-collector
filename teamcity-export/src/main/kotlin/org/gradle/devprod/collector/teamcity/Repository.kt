package org.gradle.devprod.collector.teamcity

import java.time.Instant

interface Repository {
    fun getBuildById(id: String): TeamCityBuild?

    fun storeBuild(build: TeamCityBuild)

    fun latestFinishedBuildTimestamp(projectIdPrefix: String): Instant?
}
