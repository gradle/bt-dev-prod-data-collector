package org.gradle.devprod.collector

import org.gradle.devprod.collector.teamcity.TeamcityClientService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class TestController(
    private val teamcityClientService: TeamcityClientService
) {
    @RequestMapping("/trigger-export")
    @ResponseBody
    fun triggerExport(@RequestParam("projectId") projectId: String): String {
        Thread {
            teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint(projectId)
        }.start()
        return "OK"
    }
}