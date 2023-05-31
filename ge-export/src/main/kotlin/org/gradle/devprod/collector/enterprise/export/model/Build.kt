package org.gradle.devprod.collector.enterprise.export.model

data class Build(
    val buildId: String,
    val toolType: String,
    val agentVersion: String,
    val toolVersion: String,
    val timestamp: Long,
)
