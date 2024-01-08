package org.gradle.devprod.collector.teamcity

import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

abstract class LoggingExchangeFilterFunction : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        // Log request information
        logRequest(request)

        // Delegate to the next filter or the WebClient if it's the last in the chain
        return next.exchange(request)
            .doOnSuccess { clientResponse -> logResponse(request, clientResponse) }
    }

    open fun logRequest(request: ClientRequest) {}

    open fun logResponse(request: ClientRequest, response: ClientResponse) {}
}
