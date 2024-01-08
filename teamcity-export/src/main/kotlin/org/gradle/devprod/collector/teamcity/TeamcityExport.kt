package org.gradle.devprod.collector.teamcity

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class TeamcityExport(
    private val repo: Repository,
    private val teamcityClientService: TeamcityClientService,
    private val meterRegistry: MeterRegistry,
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
        println("Loading all GBT builds from TeamCity")

        val projectIdPrefix = "Gradle"
        updateTeamCityExportTriggerMetric(projectIdPrefix)
        teamcityClientService.loadAndStoreAllBuilds(getSinceFor(projectIdPrefix), gbtPipelines)
    }

    @Async
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 60 * 60 * 1000)
    fun loadAllGeBuilds() {
        println("Loading all GE builds from TeamCity")

        val projectIdPrefix = "Enterprise"
        updateTeamCityExportTriggerMetric(projectIdPrefix)
        teamcityClientService.loadAndStoreAllBuilds(getSinceFor(projectIdPrefix), gePipelines)
    }

    private fun updateTeamCityExportTriggerMetric(projectIdPrefix: String) {
        val instant = Instant.now().epochSecond.toDouble()
        Gauge.builder("teamcity_export_last_scheduled_trigger_seconds", instant) { _ -> instant }
            .description("Last instant since the TeamCity export was triggered")
            .strongReference(true)
            .tag("project", projectIdPrefix)
            .register(meterRegistry)
    }
}
