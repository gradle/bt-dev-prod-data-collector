package org.gradle.devprod.collector.api

import com.slack.api.webhook.Payload
import org.gradle.devprod.collector.model.BuildScanSummary

interface BuildScanRenderer {
    fun render(buildScanSummary: BuildScanSummary) : Payload
}
