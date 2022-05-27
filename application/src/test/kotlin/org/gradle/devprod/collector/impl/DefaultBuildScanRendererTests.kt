package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ImageElement
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.enterprise.export.extractor.TaskOutcome
import org.gradle.devprod.collector.enterprise.export.extractor.TaskSummary
import org.gradle.devprod.collector.enterprise.export.extractor.TestSummary
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.URI
import java.time.Instant
import kotlin.time.ExperimentalTime

@ExperimentalTime
@SpringBootTest(classes = [DefaultBuildScanRenderer::class])
class DefaultBuildScanRendererTests(@Autowired val renderer: BuildScanRenderer) {

    val baseUri = URI("http://localhost")

    val generalSummary = BuildScanSummary(
        url = "http://ge.example/s/abcdef",
        projectName = "ge",
        startTime = Instant.parse("2022-05-24T11:14:14Z"),
        endTime = Instant.parse("2022-05-24T11:22:45Z"),
        outcome = BuildScanOutcome.SUCCESS,
        tags = listOf("LOCAL", "dirty", "feature-branch", "Mac OS X"),
        tasks = ":build-agent-gradle-test-func:test --tests com.gradle.scan.plugin.test.func.data.usercode.*",
        taskSummary = TaskSummary(mapOf()),
        testSummary = TestSummary(totalCount = 214, failedCount = 1, successCount = 213, skippedCount = 0)
    )

    @Test
    fun renderSuccessOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.SUCCESS)
        val contextBlock = contextBlock(renderer.render(summary, baseUri))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/success01.png", element.imageUrl)
        Assertions.assertEquals("success", element.altText)
    }

    @Test
    fun renderFailureOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.FAILURE)
        val contextBlock = contextBlock(renderer.render(summary, baseUri))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/failure.png", element.imageUrl)
        Assertions.assertEquals("failure", element.altText)
    }

    @Test
    fun renderUnknownOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.UNKNOWN)
        val contextBlock = contextBlock(renderer.render(summary, baseUri))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/unknown.png", element.imageUrl)
        Assertions.assertEquals("unknown", element.altText)
    }

    @Test
    fun renderProject() {
        val contextBlock = contextBlock(renderer.render(generalSummary, baseUri))
        Assertions.assertEquals("Build for ge", (contextBlock.elements[1] as PlainTextObject).text)
    }

    @Test
    fun rendersStartTime() {
        val contextBlock = contextBlock(renderer.render(generalSummary, baseUri))
        Assertions.assertEquals("started at 2022-05-24 11:14:14 UTC", (contextBlock.elements[2] as PlainTextObject).text)
    }

    @Test
    fun rendersDuration() {
        val contextBlock = contextBlock(renderer.render(generalSummary, baseUri))
        Assertions.assertEquals("ran for 8m 31s.", (contextBlock.elements[3] as PlainTextObject).text)
    }

    @Test
    fun rendersTagsPresent() {
        val contextBlock = contextBlock(renderer.render(generalSummary, baseUri))
        Assertions.assertEquals("Tags: LOCAL | dirty | feature-branch | Mac OS X", (contextBlock.elements[4] as MarkdownTextObject).text)
    }

    @Test
    fun rendersTagsAbsent() {
        val contextBlock = contextBlock(renderer.render(generalSummary.copy(tags = listOf()), baseUri))
        Assertions.assertEquals("_none_", (contextBlock.elements[4] as MarkdownTextObject).text)
    }

    @Test
    fun rendersTasks() {
        // TODO: consider truncating
        val textObj = markdownText(sectionBlock(renderer.render(generalSummary, baseUri), 1))
        Assertions.assertEquals("Tasks `:build-agent-gradle-test-func:test --tests com.gradle.scan.plugin.test.func.data.usercode.*`", textObj.text)
    }

    @Test
    fun rendersTaskSummarySuccess() {
        // TODO: proper pluralization
        val summary = generalSummary.copy(taskSummary = TaskSummary(mapOf(Pair(TaskOutcome.SUCCESS, 2))))
        val textObj = markdownText(sectionBlock(renderer.render(summary, baseUri), 2))
        Assertions.assertEquals("2 tasks executed", textObj.text)
    }

    @Test
    fun rendersTaskSummaryFailure() {
        // TODO: proper pluralization
        val summary = generalSummary.copy(taskSummary = TaskSummary(mapOf(Pair(TaskOutcome.SUCCESS, 2), Pair(TaskOutcome.FAILED, 1))))
        val textObj = markdownText(sectionBlock(renderer.render(summary, baseUri), 2))
        Assertions.assertEquals("3 tasks executed, <http://ge.example/s/abcdef/timeline?outcome=FAILED|1 failed tasks>", textObj.text)
    }

    @Test
    fun rendersTestSummarySuccess() {
        // TODO: proper pluralization
        val summary = generalSummary.copy(testSummary = TestSummary(totalCount = 214, failedCount = 0, successCount = 214, skippedCount = 0))
        val textObj = markdownText(sectionBlock(renderer.render(summary, baseUri), 3))
        Assertions.assertEquals("214 tests executed", textObj.text)
    }

    @Test
    fun rendersTestSummaryFailure() {
        // TODO: proper pluralization
        val summary = generalSummary.copy(testSummary = TestSummary(totalCount = 214, failedCount = 1, successCount = 213, skippedCount = 0))
        val textObj = markdownText(sectionBlock(renderer.render(summary, baseUri), 3))
        Assertions.assertEquals("214 tests executed, <http://ge.example/s/abcdef/tests/overview?outcome=failed|1 tests failed>", textObj.text)
    }

    fun contextBlock(result: UnfurlDetail) : ContextBlock {
        return result.blocks[0] as ContextBlock
    }

    fun sectionBlock(result: UnfurlDetail, index: Int) : SectionBlock {
        return result.blocks[index] as SectionBlock
    }

    fun markdownText(block: SectionBlock) : MarkdownTextObject {
        return block.text as MarkdownTextObject
    }
}
