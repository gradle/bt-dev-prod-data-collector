package org.gradle.devprod.collector

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.servlet.http.HttpServletRequest
import org.gradle.devprod.collector.api.LinkSharedHandler
import org.gradle.devprod.collector.enterprise.export.GradleEnterpriseServer
import org.gradle.devprod.collector.model.LinkSharedEventCallback
import org.gradle.devprod.collector.teamcity.TeamcityClientService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(GradleEnterpriseServer::class)
@Controller
class DeveloperProductivityDataCollector(
    private val handler: LinkSharedHandler,
    private val teamcityClientService: TeamcityClientService
) {
    @Bean
    fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.modulesToInstall(KotlinModule())
        }

    @Bean
    fun taskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.setPoolSize(4) // for 4 tasks in `TeamcityExport`
        scheduler.setThreadNamePrefix("scheduled-task-")
        scheduler.initialize()
        return scheduler
    }

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerKotlinModule()

    @RequestMapping("/slack/build-scan-previews")
    @ResponseBody
    fun buildScanPreview(@RequestBody(required = false) body: String?, request: HttpServletRequest): String {
        if (body?.contains("challenge") == true) {
            // https://api.slack.com/events/url_verification
            return objectMapper.readTree(body).get("challenge").asText()
        } else if (body?.contains("event_callback") == true) {
            val eventCallback = objectMapper.readValue<LinkSharedEventCallback>(body.toString())
            handler.handleBuildScanLinksShared(
                eventCallback.event,
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUri()
            )
            return "OK"
        } else {
            // TODO: parse data?
            return "Method: ${request.method}, body: $body"
        }
    }

    @RequestMapping("/trigger-export")
    fun triggerExport() {
        Thread {
            teamcityClientService.loadAndStoreBuildsSinceLastCheckpoint("Enterprise_Main")
        }.start()
    }
}

fun main(args: Array<String>) {
    runApplication<DeveloperProductivityDataCollector>(*args)
}
