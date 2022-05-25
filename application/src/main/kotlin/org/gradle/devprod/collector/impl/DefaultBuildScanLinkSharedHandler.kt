package org.gradle.devprod.collector.impl

import com.slack.api.model.event.LinkSharedEvent
import org.gradle.devprod.collector.api.BuildScanLinkSharedHandler
import org.springframework.stereotype.Service

@Service
class DefaultBuildScanLinkSharedHandler : BuildScanLinkSharedHandler {

    override fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent) {
        println("Handling event: $linkSharedEvent")
    }

}
