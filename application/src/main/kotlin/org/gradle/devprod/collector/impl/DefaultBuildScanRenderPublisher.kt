package org.gradle.devprod.collector.impl

import com.slack.api.Slack
import com.slack.api.methods.request.chat.ChatUnfurlRequest
import com.slack.api.methods.request.chat.ChatUnfurlRequest.ChatUnfurlRequestBuilder
import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import org.gradle.devprod.collector.api.BuildScanRenderPublisher
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanRenderPublisher : BuildScanRenderPublisher {
    private val slack = Slack.getInstance()
    private val token = System.getenv("SLACK_API_TOKEN")

    override fun publish(channel: String, timeStamp: String, unfurledLinks: Map<String, UnfurlDetail>) {
        val methods = slack.methods(token)

        val request = ChatUnfurlRequest.builder()
            .channel(channel)
            .ts(timeStamp)
            .unfurls(unfurledLinks)
            .build()

        val response = methods.chatUnfurl(request)
        if (response.isOk) {
            println("Unfurled message request was successful.")
        } else {
            println("Unfurled message request failed: ${response.error}. Full response: $response")
        }
    }
}
