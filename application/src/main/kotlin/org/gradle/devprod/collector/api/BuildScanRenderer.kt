package org.gradle.devprod.collector.api

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import org.gradle.devprod.collector.model.BuildScanSummary
import java.net.URI
import kotlin.time.ExperimentalTime

@ExperimentalTime
interface BuildScanRenderer {
    fun render(buildScanSummary: BuildScanSummary, baseUri: URI): UnfurlDetail
}
