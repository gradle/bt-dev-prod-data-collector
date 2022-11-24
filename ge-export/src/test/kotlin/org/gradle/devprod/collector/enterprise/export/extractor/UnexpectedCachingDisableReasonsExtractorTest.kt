package org.gradle.devprod.collector.enterprise.export.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnexpectedCachingDisableReasonsExtractorTest {
    @Test
    fun `can extract unexpected caching disabled reasons`() {
        val events = parse("/ngapnu6jwslkk.txt")

        assertEquals(
            listOf("OVERLAPPING_OUTPUTS"),
            UnexpectedCachingDisableReasonsExtractor.extractFrom(events.groupBy { it.eventType })
        )
    }
}
