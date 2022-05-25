package org.gradle.devprod.collector.enterprise.export.model.api

class BuildCachePerformance(
    val id: String,
    val buildTime: Long,
    val effectiveTaskExecutionTime: Long,
    val serialTaskExecutionTime: Long,
    val taskExecution: List<TaskExecution>

    )