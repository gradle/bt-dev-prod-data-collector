package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import org.gradle.devprod.collector.api.BuildScanRenderPublisher
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanRenderPublisher : BuildScanRenderPublisher {
    override fun publish(unfurlDetail: UnfurlDetail) {
        TODO("Not yet implemented")
    }
}
