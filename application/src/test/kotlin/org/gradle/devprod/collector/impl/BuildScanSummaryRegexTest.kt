package org.gradle.devprod.collector.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URI

class BuildScanSummaryRegexTest {
    @Test
    fun `summary url pattern works`() {
        Assertions.assertTrue(URI.create("https://e.grdev.net/s/yscmg42r6yc2i").path.matches(summaryPathRegex))
        Assertions.assertFalse(URI.create("https://e.grdev.net/s/4ljdi7pfriqgs/timeline").path.matches(summaryPathRegex))
    }
}
