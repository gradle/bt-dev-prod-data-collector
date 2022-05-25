package org.gradle.devprod.collector.enterprise.export

import org.gradle.devprod.collector.DeveloperProductivityDataCollector
import org.gradle.devprod.collector.model.UrlVerificationEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(
    classes = arrayOf(DeveloperProductivityDataCollector::class),
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DeveloperProductivityDataCollectorTests(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun whenChallenged_thenRespondsWithChallengeToken() {
        val result = restTemplate.postForEntity("/slack/build-scan-previews", UrlVerificationEvent("", "123abc", ""), String::class.java)
        assertEquals("123abc", result.body)
    }
}
