package org.gradle.devprod.collector.enterprise.export.extractor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestSummaryExtractorTest {

    @Test
    fun `can select out events`() {
        val events = parse("/5ui4aufy5k7e4.json")
        val preconditionTests = PreconditionTestsExtractor.extractFrom(events.groupBy { it.eventType })

        preconditionTests.forEach(::println)

        assertTrue(preconditionTests.isNotEmpty())
    }
}