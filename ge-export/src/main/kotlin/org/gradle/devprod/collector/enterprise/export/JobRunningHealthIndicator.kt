package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.Job
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

// @Component
class JobRunningHealthIndicator(private val job: Job) : HealthIndicator {
    private val messageKey = "Stream to database job"

    override fun health(): Health =
        if (job.isActive) {
            Health.up().withDetail(messageKey, "Running").build()
        } else {
            Health.down().withDetail(messageKey, "Not Running").build()
        }
}
