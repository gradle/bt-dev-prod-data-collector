package org.gradle.devprod.collector.enterprise.export

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class DatabaseCleanupHealthIndicator(
    private val databaseCleanupService: DatabaseCleanupService,
) : HealthIndicator {
    private val databaseCleanupServiceMessageKey = "Database cleanup service"

    override fun health(): Health {
        return if (databaseCleanupService.healthy) {
            Health.up().withDetail(databaseCleanupServiceMessageKey, "Healthy").build()
        } else {
            Health.down().withDetail(databaseCleanupServiceMessageKey, "Unhealthy").build()
        }
    }
}
