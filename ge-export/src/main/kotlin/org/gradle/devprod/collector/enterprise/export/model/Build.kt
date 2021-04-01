package org.gradle.devprod.collector.enterprise.export.model

data class Build(val buildId: String, val pluginVersion: String, val gradleVersion: String, val timestamp: Long)
