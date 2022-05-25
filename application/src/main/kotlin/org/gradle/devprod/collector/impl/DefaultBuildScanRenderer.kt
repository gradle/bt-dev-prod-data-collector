package org.gradle.devprod.collector.impl

import com.slack.api.webhook.Payload
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.model.BuildScanSummary
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanRenderer : BuildScanRenderer {
    override fun render(buildScanSummary: BuildScanSummary): Payload {
        TODO("Not yet implemented")
    }
}
