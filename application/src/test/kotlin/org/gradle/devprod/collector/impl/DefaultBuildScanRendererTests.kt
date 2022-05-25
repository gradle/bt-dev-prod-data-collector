package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.enterprise.export.extractor.TaskSummary
import org.gradle.devprod.collector.enterprise.export.extractor.TestSummary
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
@Disabled
class DefaultBuildScanRendererTests(@Autowired val renderer: BuildScanRenderer) {

    @Test
    fun renders() {
        val summary = BuildScanSummary(
            "ge",
            Instant.parse("2022-05-24T11:14:14Z"),
            Instant.parse("2022-05-24T11:22:45Z"),
            BuildScanOutcome.SUCCESS,
            listOf("LOCAL", "dirty", "feature-branch", "Mac OS X"),
            ":build-agent-gradle-test-func:test --tests com.gradle.scan.plugin.test.func.data.usercode.*",
            TaskSummary(mapOf()),
            TestSummary(totalCount = 214, failedCount = 1, successCount = 213, skippedCount = 0)
        )
        val result = renderer.render(summary)
        Assertions.assertEquals("TODO", result)
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
