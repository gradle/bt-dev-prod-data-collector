package org.gradle.devprod.collector.enterprise.export

import org.gradle.devprod.collector.DeveloperProductivityDataCollector
import org.gradle.devprod.collector.api.LinkSharedHandler
import org.gradle.devprod.collector.api.BuildScanSummaryService
import org.gradle.devprod.collector.model.Link
import org.gradle.devprod.collector.model.LinkSharedEvent
import org.gradle.devprod.collector.model.UrlVerificationEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import java.net.URI

@SpringBootTest(
    classes = [DeveloperProductivityDataCollector::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DeveloperProductivityDataCollectorTests(@Autowired val restTemplate: TestRestTemplate) {

    @MockBean lateinit var mockLinkSharedHandler: LinkSharedHandler
    @MockBean lateinit var mockBuildScanSummaryService: BuildScanSummaryService

    @Test
    fun whenChallenged_thenRespondsWithChallengeToken() {
        val result = restTemplate.postForEntity("/slack/build-scan-previews", UrlVerificationEvent("", "123abc", ""), String::class.java)
        assertEquals("123abc", result.body)
    }

    @Test
    fun whenLinkSharedEventReceived_thenInvokesLinkSharedHandler() {
        val result = restTemplate.postForEntity(
            "/slack/build-scan-previews",
            """
            {
                "token": "XXYYZZ",
                "team_id": "TXXXXXXXX",
                "api_app_id": "AXXXXXXXXX",
                "event": {
                    "type": "link_shared",
                    "channel": "Cxxxxxx",
                    "is_bot_user_member": true,
                    "user": "Uxxxxxxx",
                    "message_ts": "123456789.9875",
                    "unfurl_id": "C123456.123456789.987501.1b90fa1278528ce6e2f6c5c2bfa1abc9a41d57d02b29d173f40399c9ffdecf4b",
                    "thread_ts": "123456621.1855",
                    "source": "conversations_history",
                    "links": [
                        {
                            "domain": "e.grdev.net",
                            "url": "https://e.grdev.net/s/thr7hw5vzjrwi"
                        },
                        {
                            "domain": "ge.gradle.org",
                            "url": "https://ge.gradle.org/s/ei75aul4e7dg4"
                        }
                    ]
                },
                "type": "event_callback",
                "authed_users": [
                    "UXXXXXXX1",
                    "UXXXXXXX2"
                ],
                "event_id": "Ev08MFMKH6",
                "event_time": 123456789
            }                
            """.trimIndent(),
            String::class.java)

        assertTrue(result.statusCode.is2xxSuccessful)
        verify(mockLinkSharedHandler).handleBuildScanLinksShared(
            LinkSharedEvent(
                channel ="Cxxxxxx",
                user = "Uxxxxxxx",
                botUserMember = true,
                messageTs = "123456789.9875",
                unfurlId = "C123456.123456789.987501.1b90fa1278528ce6e2f6c5c2bfa1abc9a41d57d02b29d173f40399c9ffdecf4b",
                threadTs = "123456621.1855",
                source = "conversations_history",
                links = listOf(
                    Link("e.grdev.net", URI("https://e.grdev.net/s/thr7hw5vzjrwi")),
                    Link("ge.gradle.org", URI("https://ge.gradle.org/s/ei75aul4e7dg4")),
                )
            )
        )
    }
}
