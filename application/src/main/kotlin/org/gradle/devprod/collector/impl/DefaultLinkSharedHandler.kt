package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanRenderPublisher
import org.gradle.devprod.collector.api.BuildScanRenderer
import org.gradle.devprod.collector.api.BuildScanSummaryService
import org.gradle.devprod.collector.api.LinkSharedHandler
import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.time.ExperimentalTime

internal val summaryPathRegex = "/s/\\w+".toRegex()

@Service
@ExperimentalTime
class DefaultLinkSharedHandler
@Autowired constructor(
    private val buildScanSummaryService: BuildScanSummaryService,
    private val buildScanRenderer: BuildScanRenderer,
    private val buildScanRenderPublisher: BuildScanRenderPublisher
) : LinkSharedHandler {
    override fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent) {
        linkSharedEvent.links.forEach {
            val path = it.url.path.toString()
            if (path.matches(summaryPathRegex)) {
                val buildScanId = it.url.path.toString().substring("/s".length)
                val buildScanSummary = buildScanSummaryService.getSummary(it.domain, buildScanId)
                println("Build scan summary: $buildScanSummary")
                val renderedSummary = buildScanRenderer.render(buildScanSummary)
                println("Rendered scan summary: $renderedSummary")
                buildScanRenderPublisher.publish(renderedSummary)
            } else {
                println("Unsupported url: ${it.url}")
            }
        }
    }
}
