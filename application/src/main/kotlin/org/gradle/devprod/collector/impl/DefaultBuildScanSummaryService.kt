package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanSummaryService
import org.gradle.devprod.collector.model.BuildScanSummary
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanSummaryService : BuildScanSummaryService {
    override fun getSummary(geServer: String, buildScanId: String): BuildScanSummary {
        TODO("Not yet implemented")
    }
}
