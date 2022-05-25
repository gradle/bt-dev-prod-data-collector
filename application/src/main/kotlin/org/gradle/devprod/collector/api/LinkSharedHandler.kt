package org.gradle.devprod.collector.api

import org.gradle.devprod.collector.model.LinkSharedEvent
import org.springframework.scheduling.annotation.Async
import java.net.URI

interface LinkSharedHandler {

    @Async
    fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent, baseUri: URI)
}
