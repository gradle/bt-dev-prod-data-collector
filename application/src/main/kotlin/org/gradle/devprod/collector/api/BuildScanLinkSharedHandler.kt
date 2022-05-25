package org.gradle.devprod.collector.api

import com.slack.api.model.event.LinkSharedEvent
import org.springframework.scheduling.annotation.Async

interface BuildScanLinkSharedHandler {

    @Async
    fun handleBuildScanLinksShared(linkSharedEvent: LinkSharedEvent)

}
