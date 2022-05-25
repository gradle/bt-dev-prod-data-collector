package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanSummaryService
import org.gradle.devprod.collector.api.LinkSharedHandler
import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DefaultLinkSharedHandler @Autowired constructor(private val buildScanSummaryService: BuildScanSummaryService) : LinkSharedHandler {
    override fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent) {
        println("Handling event: $linkSharedEvent")
        linkSharedEvent.links.forEach {
            val buildScanId = it.url.path.toString().substring("/s".length)
            println(buildScanSummaryService.getSummary(it.domain, buildScanId))
        }
    }
}
