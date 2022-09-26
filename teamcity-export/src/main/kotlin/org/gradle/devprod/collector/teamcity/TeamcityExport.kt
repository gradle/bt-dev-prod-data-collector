package org.gradle.devprod.collector.teamcity

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class TeamcityExport(
    private val repo: Repository,
    private val teamcityClientService: TeamcityClientService
) {
    @Async
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun loadTriggerBuilds() {
        println("Loading trigger builds from Teamcity")
        teamcityClientService.loadTriggerBuilds().forEach { build -> repo.storeBuild(build) }
    }

    @Async
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun loadFailedBuilds() {
        println("Loading failed builds from Teamcity")
        val latestFailedBuildTimestamp = repo.latestFailedBuildTimestamp()

        val since = latestFailedBuildTimestamp?.minus(1, ChronoUnit.DAYS)
            ?: Instant.now().minus(5, ChronoUnit.DAYS)

        teamcityClientService.loadFailedBuilds(since).forEach { build -> repo.storeBuild(build) }
    }
}
