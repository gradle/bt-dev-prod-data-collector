package org.gradle.devprod.collector

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.gradle.devprod.collector.enterprise.export.GradleEnterpriseServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(GradleEnterpriseServer::class)
@Controller
class DeveloperProductivityDataCollector {
    @Bean
    fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.modulesToInstall(KotlinModule())
        }

    @RequestMapping("/slack/build-scan-previews")
    @ResponseBody
    fun buildScanPreview(@RequestBody(required = false) body: String?, request: HttpServletRequest): String {
        return "Method: ${request.method}, body: $body"
    }
}

fun main(args: Array<String>) {
    runApplication<DeveloperProductivityDataCollector>(*args)
}
