package org.gradle.devprod.collector.enterprise.export.model

import java.time.Duration

data class TaskStarted(val path: String, val startedAfter: Duration)