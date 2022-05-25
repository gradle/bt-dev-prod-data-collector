package org.gradle.devprod.collector.impl

import org.gradle.devprod.collector.api.LinkSharedHandler
import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.stereotype.Service

@Service
class DefaultLinkSharedHandler : LinkSharedHandler {

    override fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent) {
        println("Handling event: $linkSharedEvent")
    }

}
