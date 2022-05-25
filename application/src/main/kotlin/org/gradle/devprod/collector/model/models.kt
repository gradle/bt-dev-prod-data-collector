package org.gradle.devprod.collector.model

import org.gradle.devprod.collector.enterprise.export.extractor.TaskSummary
import org.gradle.devprod.collector.enterprise.export.extractor.TestSummary
import java.time.Instant

enum class BuildScanOutcome {
    SUCCESS, FAILURE, UNKNOWN
}

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
