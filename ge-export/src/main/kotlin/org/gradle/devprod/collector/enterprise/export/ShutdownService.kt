package org.gradle.devprod.collector.enterprise.export

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ShutdownService(private val applicationContext: ApplicationContext) {

    fun shutdown() {
        SpringApplication.exit(applicationContext)
    }
}
