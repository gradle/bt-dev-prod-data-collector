package org.gradle.devprod.collector.enterprise.export.extractor;

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FlakyTestClassExtractorTest {
    @Test
    fun `can extract flaky test classes`() {
        val events = parse("/BuildWithFlakyTests_TestStarted_TestFinished.txt")
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events.groupBy { it.eventType })
        Assertions.assertEquals(setOf("org.gradle.ide.visualstudio.plugins.VisualStudioPluginTest"), flakyTestClasses)
    }
}
