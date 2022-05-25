package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.ImageElement
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

@ExperimentalTime
@Service
class DefaultBuildScanRenderer : BuildScanRenderer {

    override fun render(buildScanSummary: BuildScanSummary, baseUri: URI): UnfurlDetail {
        val utc = ZoneId.of("UTC")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val formattedStart = dateTimeFormatter.format(buildScanSummary.startTime.atZone(utc))
        val duration = Duration.between(buildScanSummary.startTime, buildScanSummary.endTime).toKotlinDuration()
        return UnfurlDetail.builder().blocks(listOf(
            ContextBlock.builder().elements(listOf(
                buildOutcomeImage(buildScanSummary.outcome, baseUri),
                BlockCompositions.plainText("Project: ${buildScanSummary.projectName}"),
                BlockCompositions.plainText("Start: $formattedStart"),
                BlockCompositions.plainText("Duration: $duration"),
                BlockCompositions.plainText("Tags: ${buildScanSummary.tags.stream().collect(Collectors.joining(" | "))}")
            )).build()
        )).build()
    }

    fun buildOutcomeImage(outcome: BuildScanOutcome, baseUri: URI): ImageElement {
        return when (outcome) {
            BuildScanOutcome.SUCCESS -> buildImageElement("success.png", "success", baseUri)
            BuildScanOutcome.FAILURE -> buildImageElement("failure.png", "failure", baseUri)
            BuildScanOutcome.UNKNOWN -> buildImageElement("unknown.png", "unknown", baseUri)
        }
    }

    fun buildImageElement(fileName: String, altText: String, baseUri: URI) : ImageElement {
        return ImageElement.builder()
            .imageUrl(UriComponentsBuilder.fromUri(baseUri).path(fileName).toUriString())
            .altText(altText)
            .build()
    }
}
