package org.gradle.devprod.collector.enterprise.export.model.api

data class Build(
    val id: String,
    val availableAt: Long,
    val buildToolType: String,
    val buildToolVersion: String,
    val buildAgentVersion: String
)