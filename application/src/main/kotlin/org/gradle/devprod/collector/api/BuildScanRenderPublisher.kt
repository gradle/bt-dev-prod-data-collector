package org.gradle.devprod.collector.api

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail

interface BuildScanRenderPublisher {
    fun publish(channel: String, timeStamp: String, unfurledLinks: Map<String, UnfurlDetail>)
}
