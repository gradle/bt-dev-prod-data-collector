package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.api.LinkSharedHandler
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
class DefaultLinkSharedHandlerTests(@Autowired val linkSharedHandler: LinkSharedHandler) {

    @Test
    fun renders() {
    }
}
