package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.gradle.devprod.collector.enterprise.export.model.Build
import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

private const val MAX_RECONNECT_COUNT = 2

@Service
class ExportApiClient(private val server: GradleEnterpriseServer) {
    private val client: WebClient = WebClient.builder()
        .baseUrl("${server.url}/build-export")
        .codecs { it.defaultCodecs().maxInMemorySize(512 * 1024) }
        .build()
    private val lastStreamEventId = AtomicReference("")
    private val reconnectCount = AtomicInteger(0)
    private val logger = LoggerFactory.getLogger(javaClass)

    fun createEventStream(): Flow<ServerSentEvent<Build?>> =
        createEventStreamFlux().asFlow().onEach { lastStreamEventId.set(it.id()) }

    private fun createEventStreamFlux(): Flux<ServerSentEvent<Build?>> =
        client
            .get()
            .uri("/v2/builds/since/${System.currentTimeMillis() - 72 * 60 * 60 * 1000}?stream")
            .setLastEventId()
            .bearerAuth()
            .retrieve()
            .bodyToFlux<ServerSentEvent<Build?>>()
            .onErrorResume { throwable ->
                logger.error("Failure with /builds/since export API", throwable)
                if (reconnectCount.getAndIncrement() >= MAX_RECONNECT_COUNT) {
                    throw IllegalStateException(
                        "Failed to connect after $MAX_RECONNECT_COUNT retries", throwable
                    )
                } else {
                    createEventStreamFlux().apply { reconnectCount.set(0) }
                }
            }

    private fun WebClient.RequestHeadersSpec<*>.bearerAuth() =
        header(
            "Authorization",
            "Bearer ${Base64.getEncoder().encodeToString(server.apiToken.toByteArray())}"
        )

    // https://docs.gradle.com/enterprise/export-api/#reconnect_resume
    // https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
    private fun WebClient.RequestHeadersSpec<*>.setLastEventId(): WebClient.RequestHeadersSpec<*> {
        val lastStreamEventId = lastStreamEventId.get() ?: ""
        if (lastStreamEventId.isNotEmpty()) {
            header("Last-Event-ID", lastStreamEventId)
        }
        return this
    }

    fun getEvents(build: Build, events: List<String>): Flow<ServerSentEvent<BuildEvent>> = getEvents(build.buildId, events)
    fun getEvents(buildId: String, events: List<String>): Flow<ServerSentEvent<BuildEvent>> =
        client
            .get()
            .uri("/v2/build/$buildId/events?eventTypes=${events.joinToString(",")}")
            .bearerAuth()
            .retrieve()
            .bodyToFlow()
}
