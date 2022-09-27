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

    private val gbtPipelines = listOf("Master", "Release")
    private val gbtBuildConfigurations: (String) -> List<String> = {
        pipeline: String ->
        listOf(
            "Gradle_${pipeline}_Check_Stage_QuickFeedbackLinuxOnly_Trigger",
            "Gradle_${pipeline}_Check_Stage_QuickFeedback_Trigger",
            "Gradle_${pipeline}_Check_Stage_PullRequestFeedback_Trigger",
            "Gradle_${pipeline}_Check_Stage_ReadyforNightly_Trigger",
            "Gradle_${pipeline}_Check_Stage_ReadyforRelease_Trigger"
        )
    }
    private val gbtRootProjectAffectedBuild: (String) -> String = {
        pipeline: String ->
        "Gradle_${pipeline}_Check"
    }

    private val gePipelines = listOf("Main", "Release")
    private val geRootProjectAffectedBuild: (String) -> String = {
        pipeline: String ->
        "Enterprise_$pipeline"
    }

    @Async
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun loadGbtTriggerBuilds() {
        println("Loading trigger builds from Teamcity")
        teamcityClientService.loadTriggerBuilds(gbtPipelines, gbtBuildConfigurations).forEach { build -> repo.storeBuild(build) }
    }

    @Async
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun loadGbtFailedBuilds() {
        println("Loading failed GBT builds from Teamcity")
        val latestFailedBuildTimestamp = repo.latestFailedBuildTimestamp("Gradle")

        val since = latestFailedBuildTimestamp?.minus(1, ChronoUnit.DAYS)
            ?: Instant.now().minus(5, ChronoUnit.DAYS)

        teamcityClientService.loadFailedBuilds(since, gbtPipelines, gbtRootProjectAffectedBuild).forEach { build -> repo.storeBuild(build) }
    }

    @Async
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun loadGeFailedBuilds() {
        println("Loading failed GE builds from Teamcity")
        val latestFailedBuildTimestamp = repo.latestFailedBuildTimestamp("Enterprise")

        val since = latestFailedBuildTimestamp?.minus(1, ChronoUnit.DAYS)
            ?: Instant.now().minus(5, ChronoUnit.DAYS)

        teamcityClientService.loadFailedBuilds(since, gePipelines, geRootProjectAffectedBuild).forEach { build -> repo.storeBuild(build) }
    }
}
