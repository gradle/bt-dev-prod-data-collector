package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.gradle.devprod.collector.enterprise.export.model.api.Build
import org.gradle.devprod.collector.enterprise.export.model.api.BuildCachePerformance
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

@Service
class GeApiClient(private val server: GradleEnterpriseServer) {
    private val client: WebClient = WebClient.builder().baseUrl("${server.url}/api")
        .codecs { it.defaultCodecs().maxInMemorySize(2048 * 1024) }
        .build()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun readBuilds(): Flow<Build> =
        client.get()
            .uri("/builds?since=${System.currentTimeMillis() - 72 * 60 * 60 * 1000}")
            .bearerAuth()
            .retrieve()
            .bodyToFlux<Build>()
            .asFlow()

        private fun WebClient.RequestHeadersSpec<*>.bearerAuth() =
            header(
                "Authorization",
                "Bearer ${server.apiToken}"
            )

    fun readBuildCachePerfomanceData(build: Build) =
        client.get()
            .uri("/builds/${build.id}/gradle-build-cache-performance")
            .bearerAuth()
            .retrieve()
            .bodyToFlux<BuildCachePerformance>()
            .asFlow()
}