package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.BlockCompositions
import com.slack.api.model.block.element.ImageElement
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.enterprise.export.extractor.TaskOutcome
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.springframework.stereotype.Service
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

    // TODO: see https://api.slack.com/reference/surfaces/formatting#escaping for escaping rules

    override fun render(buildScanSummary: BuildScanSummary, baseUri: URI): UnfurlDetail {
        val utc = ZoneId.of("UTC")
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val formattedStart = dateTimeFormatter.format(buildScanSummary.startTime.atZone(utc))
        val duration = Duration.between(buildScanSummary.startTime, buildScanSummary.endTime).toKotlinDuration()
        val successTaskCount = buildScanSummary.taskSummary.outcomes[TaskOutcome.SUCCESS] ?: 0
        val failedTaskCount = buildScanSummary.taskSummary.outcomes[TaskOutcome.FAILED] ?: 0
        val executedTaskCount = successTaskCount + failedTaskCount
        var taskSummaryMessage = "$executedTaskCount tasks executed"
        if (failedTaskCount > 0) {
            // TODO: link
            //val failedTaskUrl = UriComponentsBuilder.fromUri(buildScanSummary)
            // TODO: escaping
            //taskSummaryMessage += ", <${failedTaskUrl}|${failedTaskCount failed tasks}>"
            taskSummaryMessage += ", $failedTaskCount failed tasks"
        }
        val failedTestCount = buildScanSummary.testSummary.failedCount
        var testSummaryMessage = "${buildScanSummary.testSummary.totalCount} tests executed"
        if (failedTestCount > 0) {
            // TODO: link
            testSummaryMessage += ", ${failedTestCount} tests failed"
        }
        return UnfurlDetail.builder().blocks(listOf(
            ContextBlock.builder().elements(listOf(
                buildOutcomeImage(buildScanSummary.outcome, baseUri),
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
                .build(),
            SectionBlock.builder()
                // TODO: escaping
                .text(BlockCompositions.markdownText(taskSummaryMessage))
                .build(),
            SectionBlock.builder()
                // TODO: escaping
                .text(BlockCompositions.markdownText(testSummaryMessage))
                .build()
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
