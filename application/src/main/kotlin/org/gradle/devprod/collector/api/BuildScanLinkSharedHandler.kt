package org.gradle.devprod.collector.api

import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.scheduling.annotation.Async

interface BuildScanLinkSharedHandler {

    @Async
    fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent)

}
