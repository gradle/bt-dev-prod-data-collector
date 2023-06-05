package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.Job
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class JobRunningHealthIndicator(
    private val job: Job,
    private val databaseCleanupService: DatabaseCleanupService,
) : HealthIndicator {
    private val streamToDatabaseJobMessageKey = "Stream to database job"
    private val databaseCleanupServiceMessageKey = "Database cleanup service"

    override fun health(): Health {
        val builder = if (job.isActive && databaseCleanupService.healthy) Health.up() else Health.down()

        val details = mutableMapOf<String, String>()
        details[streamToDatabaseJobMessageKey] = if (job.isActive) "Running" else "Not Running"
        details[databaseCleanupServiceMessageKey] = if (databaseCleanupService.healthy) "Healthy" else "Unhealthy"
        return builder.withDetails(details).build()
    }
}
