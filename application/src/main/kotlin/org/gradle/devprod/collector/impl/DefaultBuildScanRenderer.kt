package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.ImageElement
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

@ExperimentalTime
@Service
class DefaultBuildScanRenderer : BuildScanRenderer {

    // TODO: see https://api.slack.com/reference/surfaces/formatting#escaping for escaping rules

    override fun render(buildScanSummary: BuildScanSummary): UnfurlDetail {
        val utc = ZoneId.of("UTC")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val formattedStart = dateTimeFormatter.format(buildScanSummary.startTime.atZone(utc))
        val duration = Duration.between(buildScanSummary.startTime, buildScanSummary.endTime).toKotlinDuration()
        return UnfurlDetail.builder().blocks(listOf(
            ContextBlock.builder().elements(listOf(
                buildOutcomeImage(buildScanSummary.outcome),
                // TODO: escaping
                BlockCompositions.plainText("Project: ${buildScanSummary.projectName}"),
                BlockCompositions.plainText("Start: $formattedStart"),
                BlockCompositions.plainText("Duration: $duration"),
                // TODO: escaping
                BlockCompositions.plainText("Tags: ${buildScanSummary.tags.stream().collect(Collectors.joining(" | "))}")
            )).build(),
            SectionBlock.builder()
                // TODO: escaping
                .text(BlockCompositions.markdownText("Tasks `${buildScanSummary.tasks}`"))
                .build()
        )).build()
    }

    fun buildOutcomeImage(outcome: BuildScanOutcome): ImageElement {
        return when (outcome) {
            BuildScanOutcome.SUCCESS -> buildImageElement("success.png", "success")
            BuildScanOutcome.FAILURE -> buildImageElement("failure.png", "failure")
            BuildScanOutcome.UNKNOWN -> buildImageElement("unknown.png", "unknown")
        }
    }

    fun buildImageElement(fileName: String, altText: String) : ImageElement {
        return ImageElement.builder()
            .imageUrl(ServletUriComponentsBuilder.fromCurrentContextPath().path(fileName).toUriString())
            .altText(altText)
            .build()
    }
}
