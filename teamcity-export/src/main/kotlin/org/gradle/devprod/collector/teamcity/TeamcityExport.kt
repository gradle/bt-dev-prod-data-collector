package org.gradle.devprod.collector.teamcity

import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

const val GRADLE_MASTER_CHECK_PROJECT_ID = "Gradle_Master_Check"
const val GRADLE_RELEASE_CHECK_PROJECT_ID = "Gradle_Release_Check"
const val ENTERPRISE_MAIN_PROJECT_ID = "Enterprise_Main"
const val ENTERPRISE_RELEASE_PROJECT_ID = "Enterprise_Release"

@Component
class TeamcityExport(
    private val teamcityClientService: TeamcityClientService,
) {
    @Async
    @Scheduled(initialDelay = 1 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    fun loadGradleMasterBuilds() {
        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint(GRADLE_MASTER_CHECK_PROJECT_ID)
    }

    @Async
    @Scheduled(initialDelay = 2 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    fun loadGradleReleaseBuilds() {
        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint(GRADLE_RELEASE_CHECK_PROJECT_ID)
    }

    @Async
    @Scheduled(initialDelay = 3 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    fun loadDVMainBuilds() {
        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint(ENTERPRISE_MAIN_PROJECT_ID)
    }

    @Async
    @Scheduled(initialDelay = 4 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    fun loadDVReleaseBuilds() {
        teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint(ENTERPRISE_RELEASE_PROJECT_ID)
    }
}
