package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.BuildScanLinkSharedHandler
import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanLinkSharedHandler : BuildScanLinkSharedHandler {

    override fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent) {
        println("Handling event: $linkSharedEvent")
    }

}
