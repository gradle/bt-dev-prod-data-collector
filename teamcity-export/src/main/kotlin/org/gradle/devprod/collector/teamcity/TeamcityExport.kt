package org.gradle.devprod.collector.teamcity

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class TeamcityExport(
    private val repo: Repository,
    private val teamcityClientService: TeamcityClientService,
) {
    private val gbtPipelines = listOf("Gradle_Master_Check", "Gradle_Release_Check")

    private val gePipelines = listOf("Enterprise_Main", "Enterprise_Release")

    private fun getSinceFor(projectIdPrefix: String): Instant {
        val latestFinishedBuildTimestamp = repo.latestFinishedBuildTimestamp(projectIdPrefix)

        return latestFinishedBuildTimestamp?.minus(1, ChronoUnit.DAYS)
            ?: Instant.now().minus(5, ChronoUnit.DAYS)
    }

    @Async
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    fun loadAllGbtBuilds() {
        println("Loading all GBT builds from Teamcity")

        teamcityClientService.loadAndStoreAllBuilds(getSinceFor("Gradle"), gbtPipelines)
    }

    @Async
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    fun loadGeFailedBuilds() {
        println("Loading failed GE builds from Teamcity")

        teamcityClientService
            .loadFailedBuilds(getSinceFor("Enterprise"), gePipelines)
            .forEach { build -> repo.storeBuild(build) }
    }
}
