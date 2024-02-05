package org.gradle.devprod.collector

import org.gradle.devprod.collector.teamcity.TeamcityClientService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
open class TestController(
    private val teamcityClientService: TeamcityClientService
) {
    @RequestMapping("/trigger-export")
    fun triggerExport() {
        Thread {
            teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Enterprise_Main")
        }.start()
    }
}