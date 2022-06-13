package org.gradle.devprod.collector.enterprise.export.extractor;

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FlakyTestClassExtractorTest {
    @Test
    fun `can extract flaky test classes`() {
        val events = parse("/BuildWithFlakyTests_TestStarted_TestFinished_TaskStarted.txt")
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events.groupBy { it.eventType })
        Assertions.assertEquals(setOf("org.gradle.cli.FlakyTest"), flakyTestClasses)
    }

    @Test
    fun `a failed first skipped later test case is not recognized as flaky`() {
        val events = parse("/g2yl5n377k3as.txt")
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events.groupBy { it.eventType })
        Assertions.assertTrue(flakyTestClasses.isEmpty())
    }
}
