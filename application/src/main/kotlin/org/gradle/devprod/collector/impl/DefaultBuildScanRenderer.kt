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
            val failedTaskUrl = UriComponentsBuilder.fromHttpUrl(buildScanSummary.url).path("/timeline").queryParam("outcome", "FAILED").toUriString()
            taskSummaryMessage += ", <${failedTaskUrl}|${failedTaskCount} failed tasks>"
        }
        val failedTestCount = buildScanSummary.testSummary.failedCount
        var testSummaryMessage = "${buildScanSummary.testSummary.totalCount} tests executed"
        if (failedTestCount > 0) {
            val failedTestUrl = UriComponentsBuilder.fromHttpUrl(buildScanSummary.url).path("/tests/overview").queryParam("outcome", "failed").toUriString()
            testSummaryMessage += ", <${failedTestUrl}|${failedTestCount} tests failed>"
        }
        return UnfurlDetail.builder().blocks(listOf(
            ContextBlock.builder().elements(listOf(
                buildOutcomeImage(buildScanSummary.outcome, baseUri),
                BlockCompositions.plainText(escapeText(buildScanSummary.projectName)),
                BlockCompositions.markdownText(renderTags(buildScanSummary.tags))
            )).build(),
            ContextBlock.builder().elements(listOf(
                BlockCompositions.plainText("Start: $formattedStart"),
                BlockCompositions.plainText("Duration: $duration"),
            )).build(),
            SectionBlock.builder()
                .text(BlockCompositions.markdownText("Tasks `${escapeText(buildScanSummary.tasks)}`"))
                .build(),
            SectionBlock.builder()
                .text(BlockCompositions.markdownText(taskSummaryMessage))
                .build(),
            SectionBlock.builder()
                .text(BlockCompositions.markdownText(testSummaryMessage))
                .build()
        )).build()
    }

    private fun renderTags(tags: List<String>): String {
        if (tags.isEmpty()) {
            return "Tags: _none_";
        } else {
            return "Tags: ${tags.stream().map(this::escapeText).collect(Collectors.joining(" | "))}"
        }
    }

    fun buildOutcomeImage(outcome: BuildScanOutcome, baseUri: URI): ImageElement {
        return when (outcome) {
            BuildScanOutcome.SUCCESS -> buildImageElement("/success01.png", "success", baseUri)
            BuildScanOutcome.FAILURE -> buildImageElement("/failure.png", "failure", baseUri)
            BuildScanOutcome.UNKNOWN -> buildImageElement("/unknown.png", "unknown", baseUri)
        }
    }

    fun buildImageElement(fileName: String, altText: String, baseUri: URI) : ImageElement {
        return ImageElement.builder()
            .imageUrl(UriComponentsBuilder.fromUri(baseUri).path(fileName).toUriString())
            .altText(altText)
            .build()
    }

    /**
     * Replace any Slack-special markdown characters in the text with the appropriate escape codes.
     *
     * @return the escaped text
     * @see <a href="https://api.slack.com/reference/surfaces/formatting#escaping">Escaping text</a>
     */
    fun escapeText(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
