package org.gradle.devprod.collector.impl

import com.slack.api.webhook.Payload
import org.gradle.devprod.collector.api.BuildScanRenderPublisher
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanRenderPublisher : BuildScanRenderPublisher {
    override fun publish(payload: Payload) {
        TODO("Not yet implemented")
    }
}
