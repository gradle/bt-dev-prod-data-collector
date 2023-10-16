package org.gradle.devprod.collector.enterprise.export.extractor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PreconditionsTestsExtractorTest {

    @Test
    fun `allow same id under different tasks`() {
        val events = parse("/lnxw7ymnzary4.txt")
        val preconditionTests = PreconditionTestsExtractor.extractFrom(events.groupBy { it.eventType })

        assertEquals(
            listOf(
                PreconditionTest(
                    "org.gradle.test.precondition.LocalPreconditionProbingTest",
                    ":precondition-tester:test",
                    listOf(
                        "org.gradle.test.preconditions.IntegTestPreconditions\$IsEmbeddedExecutor",
                        "org.gradle.test.preconditions.UnitTestPreconditions\$Windows"
                    ),
                    TestOutcome.SKIPPED
                ),
                PreconditionTest(
                    "org.gradle.test.precondition.LocalPreconditionProbingTest",
                    ":precondition-tester:embeddedCrossVersionTest",
                    listOf(
                        "org.gradle.test.preconditions.IntegTestPreconditions\$IsEmbeddedExecutor",
                        "org.gradle.test.preconditions.UnitTestPreconditions\$Windows"
                    ),
                    TestOutcome.PASSED
                ),
            ), preconditionTests
        )
    }

    @Test
    fun `extractor smoke test`() {
        val events = parse("/czgaz3426aszo.txt")
        val preconditionTests = PreconditionTestsExtractor.extractFrom(events.groupBy { it.eventType })

        assertTrue(preconditionTests.isNotEmpty())
        preconditionTests.forEach(::println)
    }

    @Test
    fun `can extract single precondition names`() {
        val name = "Preconditions [A]"
        val preconditions = PreconditionTestsExtractor.extractPreconditionNames(name)

        assertEquals(1, preconditions.size)
        assertEquals("A", preconditions[0])
    }

    @Test
    fun `can extract multiple precondition names`() {
        val name = "Preconditions [A, B, C]"
        val preconditions = PreconditionTestsExtractor.extractPreconditionNames(name)

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
        assertThrows<IllegalArgumentException> {
            PreconditionTestsExtractor.extractPreconditionNames(name)
        }
    }
}