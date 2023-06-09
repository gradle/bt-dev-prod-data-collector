package org.gradle.devprod.collector.enterprise.export.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PreconditionsTestsExtractorTest {

    @Test
    fun `extractor smoke test`() {
        val events = parse("/expqbzdefwknc.txt")
        val preconditionTests = PreconditionExtractor.extractFrom(events.groupBy { it.eventType })

        assertTrue(preconditionTests.isNotEmpty())
        preconditionTests.forEach(::println)
    }

    @Test
    fun `can extract single precondition names`() {
        val name = "Preconditions [A]"
        val preconditions = PreconditionExtractor.extractPreconditionNames(name)

        assertEquals(1, preconditions.size)
        assertEquals("A", preconditions[0])
    }

    @Test
    fun `can extract multiple precondition names`() {
        val name = "Preconditions [A, B, C]"
        val preconditions = PreconditionExtractor.extractPreconditionNames(name)

        assertEquals(3, preconditions.size)
        assertEquals("A", preconditions[0])
        assertEquals("B", preconditions[1])
        assertEquals("C", preconditions[2])
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Preconditions [",
            "Preconditions ]",
            "Preconditions []",
            "Preconditions [,]",
        ]
    )
    fun `does not accept misformatted preconditions`(name: String) {
        assertThrows<PreconditionNameFormatException> {
            PreconditionExtractor.extractPreconditionNames(name)
        }
    }
}