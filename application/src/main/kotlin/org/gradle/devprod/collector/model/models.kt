package org.gradle.devprod.collector.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.slack.api.model.event.LinkSharedEvent
import org.gradle.devprod.collector.enterprise.export.extractor.TaskSummary
import org.gradle.devprod.collector.enterprise.export.extractor.TestSummary
import java.net.URI
import java.time.Instant

enum class BuildScanOutcome {
    SUCCESS, FAILURE, UNKNOWN
}

data class BuildScanSummary(
    val projectName: String,
    val startTime: Instant,
    val endTime: Instant,
    val outcome: BuildScanOutcome,
    val tags: List<String>,
    val tasks: String,
    val taskSummary: TaskSummary,
    val testSummary: TestSummary
)

data class LinkSharedEventCallback(
    @JsonProperty("event_id")
    val eventId: String,
    @JsonProperty("event_time")
    val eventTime: Long,
    val event: LinkSharedEvent
)

data class UrlVerificationEvent(
    val token: String,
    val challenge: String,
    val type: String
)

data class LinkSharedEvent(
    val channel: String,
    val user: String,
    @JsonProperty("message_ts")
    val messageTs: String,
    @JsonProperty("thread_ts")
    val threadTs: String,
    val links: List<Link>,
    @JsonProperty("is_bot_user_member")
    val botUserMember: Boolean,
    @JsonProperty("unfurl_id")
    val unfurlId: String,
    val source: String
)

data class Link(
    val domain: String,
    val url: URI
)
