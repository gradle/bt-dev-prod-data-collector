package org.gradle.devprod.collector.model

import java.time.Instant

enum class BuildScanOutcome {
    SUCCESS, FAILURE, UNKNOWN
}

data class TaskSummary(val totalTasks: Int)
data class TestSummary(val totalTests: Int)

data class BuildScanSummary(
    val projectName: String,
    val startTime: Instant,
    val endTime: Instant,
    val outcome: BuildScanOutcome,
    val tags: List<String>,
    val tasks: String,
    val taskSummary: TaskSummary,
    val testSummary: TestSummary
)
