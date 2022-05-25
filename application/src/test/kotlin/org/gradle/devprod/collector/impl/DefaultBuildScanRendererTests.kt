package org.gradle.devprod.collector.impl

import com.slack.api.methods.request.chat.ChatUnfurlRequest.UnfurlDetail
import com.slack.api.model.block.ContextBlock
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ImageElement
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.gradle.devprod.collector.model.TaskSummary
import org.gradle.devprod.collector.model.TestSummary
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.time.ExperimentalTime

@ExperimentalTime
@SpringBootTest
class DefaultBuildScanRendererTests(@Autowired val renderer: BuildScanRenderer) {

    val generalSummary = BuildScanSummary(
        projectName = "ge",
        startTime = Instant.parse("2022-05-24T11:14:14Z"),
        endTime = Instant.parse("2022-05-24T11:22:45Z"),
        outcome = BuildScanOutcome.SUCCESS,
        tags = listOf("LOCAL", "dirty", "feature-branch", "Mac OS X"),
        tasks = ":build-agent-gradle-test-func:test --tests com.gradle.scan.plugin.test.func.data.usercode.*",
        taskSummary = TaskSummary(1841),
        testSummary = TestSummary(214)
    )

    @Test
    fun renderSuccessOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.SUCCESS)
        val contextBlock = contextBlock(renderer.render(summary))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/success.png", element.imageUrl)
        Assertions.assertEquals("success", element.altText)
    }

    @Test
    fun renderFailureOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.FAILURE)
        val contextBlock = contextBlock(renderer.render(summary))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/failure.png", element.imageUrl)
        Assertions.assertEquals("failure", element.altText)
    }

    @Test
    fun renderUnknownOutcome() {
        val summary = generalSummary.copy(outcome = BuildScanOutcome.UNKNOWN)
        val contextBlock = contextBlock(renderer.render(summary))
        val element = contextBlock.elements[0] as ImageElement
        Assertions.assertEquals("http://localhost/unknown.png", element.imageUrl)
        Assertions.assertEquals("unknown", element.altText)
    }

    @Test
    fun renderProject() {
        val contextBlock = contextBlock(renderer.render(generalSummary))
        Assertions.assertEquals("Project: ge", (contextBlock.elements[1] as PlainTextObject).text)
    }

    @Test
    fun rendersStartTime() {
        val contextBlock = contextBlock(renderer.render(generalSummary))
        Assertions.assertEquals("Start: 2022-05-24 11:14:14 UTC", (contextBlock.elements[2] as PlainTextObject).text)
    }

    @Test
    fun rendersDuration() {
        val contextBlock = contextBlock(renderer.render(generalSummary))
        Assertions.assertEquals("Duration: 8m 31s", (contextBlock.elements[3] as PlainTextObject).text)
    }

    @Test
    fun rendersTags() {
        val contextBlock = contextBlock(renderer.render(generalSummary))
        Assertions.assertEquals("Tags: LOCAL | dirty | feature-branch | Mac OS X", (contextBlock.elements[4] as PlainTextObject).text)
    }

    // TODO: more

    fun contextBlock(result: UnfurlDetail) : ContextBlock {
        return result.blocks[0] as ContextBlock
    }

//		{
//			"type": "context",
//			"elements": [
//				{
//					"type": "image",
//					"image_url": "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Eo_circle_green_checkmark.svg/240px-Eo_circle_green_checkmark.svg.png",
//					"alt_text": "success"
//				},
//				{
//					"type": "plain_text",
//					"text": "Project: ge"
//				},
//				{
//					"type": "plain_text",
//					"text": "Start: 2022-05-25 9:14:14 AM CEST"
//				},
//				{
//					"type": "plain_text",
//					"text": "Duration: 8 min 31 sec"
//				},
//				{
//					"type": "mrkdwn",
//					"text": "Tags: LOCAL | dirty | feature-branch | Mac OS X"
//				}
//			]
//		},
//		{
//			"type": "section",
//			"text": {
//				"type": "mrkdwn",
//				"text": "Tasks `:build-agent-gradle-test-func:test --tests com.gradle.scan.plugin.test.func.data.usercode.*`"
//			}
//		},
//		{
//			"type": "section",
//			"text": {
//				"type": "mrkdwn",
//				"text": "1841 tasks executed in 340 projects, <https://e.grdev.net/s/p7laz2e6qf4bc/timeline?outcome=FAILED|1 failed task> in 47m 57s, with 471 avoided tasks saving 22m 7.679s"
//			}
//		},
//		{
//			"type": "section",
//			"text": {
//				"type": "mrkdwn",
//				"text": "214 tests executed in 44m 11s, <https://e.grdev.net/s/p7laz2e6qf4bc/tests/overview?outcome=failed|12 failed>, <https://e.grdev.net/s/p7laz2e6qf4bc/tests/overview?outcome=flaky|4 flaky>"
//			}
//		}
}
