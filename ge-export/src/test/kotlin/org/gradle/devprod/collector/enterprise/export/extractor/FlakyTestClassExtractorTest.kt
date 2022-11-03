package org.gradle.devprod.collector.enterprise.export.extractor;

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FlakyTestClassExtractorTest {
    @Test
    fun `can extract flaky test classes`() {
        val events = parse("/BuildWithFlakyTests_TestStarted_TestFinished_TaskStarted.txt")
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events.groupBy { it.eventType })
        Assertions.assertEquals(setOf("org.gradle.cli.FlakyTest"), flakyTestClasses)
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(
        value = [
            "/g2yl5n377k3as.txt, a failed first skipped later test case is not recognized as flaky",
            "/5pheg3dpbrclo.txt, take suite into consideration"
        ]
    )
    fun `regression test`(eventsResourceName: String, reason: String) {
        val events = parse(eventsResourceName)
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events.groupBy { it.eventType })
        Assertions.assertTrue(flakyTestClasses.isEmpty())
    }
}
