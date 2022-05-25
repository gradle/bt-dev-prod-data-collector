package org.gradle.devprod.collector.api

import com.slack.api.webhook.Payload

interface BuildScanRenderPublisher {
    fun publish(payload: Payload)
}
