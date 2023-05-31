package org.gradle.devprod.collector.enterprise.export.extractor

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/gradle/task/TaskOutcome_1.html
enum class TaskOutcome {
    AVOIDED_FOR_UNKNOWN_REASON,
    FAILED,
    FROM_CACHE,
    NO_SOURCE,
    SKIPPED,
    SUCCESS,
    UP_TO_DATE,
}

data class TaskSummary(val outcomes: Map<TaskOutcome, Int>)
data class TestSummary(val totalCount: Int, val failedCount: Int, val successCount: Int, val skippedCount: Int)
