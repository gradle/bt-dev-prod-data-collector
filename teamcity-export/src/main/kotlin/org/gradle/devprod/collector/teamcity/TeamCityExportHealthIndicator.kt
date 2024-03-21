package org.gradle.devprod.collector.teamcity

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Checks if the TeamCity export is lagging too far behind.
 * {@see TeamcityExport}
 */
@Component
class TeamCityExportHealthIndicator(
    private val repository: Repository,
) : HealthIndicator {
    private val monitoredProjectIds = listOf(
        GRADLE_MASTER_CHECK_PROJECT_ID,
        GRADLE_RELEASE_CHECK_PROJECT_ID,
        ENTERPRISE_MAIN_PROJECT_ID,
        ENTERPRISE_RELEASE_PROJECT_ID,
    )

    // 6 hours
    private val laggingThreshold = 6L * 60 * 60

    override fun health(): Health {
        val checkpoints = monitoredProjectIds.associateWith { repository.latestFinishedBuildTimestamp(it) }
        val now = Instant.now()

        val ret = if (checkpoints.values.any { it.plusSeconds(laggingThreshold) < now }) {
            Health.down()
        } else {
            Health.up()
        }

        checkpoints.forEach {
            ret.withDetail(it.key, it.value.toString())
        }
        return ret.build()
    }
}
