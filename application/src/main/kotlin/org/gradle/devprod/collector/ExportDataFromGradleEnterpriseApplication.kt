package org.gradle.devprod.collector

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.gradle.devprod.collector.enterprise.export.GradleEnterpriseServer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties(GradleEnterpriseServer::class)
class ExportDataFromGradleEnterpriseApplication {
	@Bean
	fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
		builder.modulesToInstall(KotlinModule())
	}
}

fun main(args: Array<String>) {
	runApplication<ExportDataFromGradleEnterpriseApplication>(*args)
}
