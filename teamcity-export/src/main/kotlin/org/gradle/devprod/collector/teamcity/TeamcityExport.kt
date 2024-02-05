package org.gradle.devprod.collector.teamcity

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TeamcityExport(
    private val teamcityClientService: TeamcityClientService
) {
//    @Async
//    @Scheduled(initialDelay = 1 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
//    fun loadGradleMasterBuilds() {
//        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Gradle_Master_Check")
//    }
//
//    @Async
//    @Scheduled(initialDelay = 2 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
//    fun loadGradleReleaseBuilds() {
//        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Gradle_Release_Check")
//    }
//
//    @Async
//    @Scheduled(initialDelay = 3 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
//    fun loadDVMainBuilds() {
//        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Enterprise_Main")
//    }
//
//    @Async
//    @Scheduled(initialDelay = 4 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
//    fun loadDVReleaseBuilds() {
//        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Enterprise_Release")
//    }
}
