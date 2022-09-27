package org.gradle.devprod.collector.teamcity

import java.time.Instant

interface Repository {
    fun storeBuild(build: TeamCityBuild)

    fun latestFailedBuildTimestamp(buildNamePrefix: String): Instant?
}
