package org.gradle.devprod.collector.enterprise.export.model.api

class TaskExecution(
    val taskPath: String,
    val avoidanceOutcome: String,
    val duration: Long,
    val fingerprintDuration: Long,
    val avoidanceSavings: Long

    )