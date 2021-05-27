package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent

abstract class Extractor<T>(val eventTypes: List<String>) {
    open fun extract(events: Iterable<BuildEvent>): T {
        throw NotImplementedError()
    }

    open fun extractFrom(events: Map<String?, List<BuildEvent>>): T {
        throw NotImplementedError()
    }
}

abstract class SingleEventExtractor<T>(eventType: String) : Extractor<T>(listOf(eventType)) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): T =
        extract(events.getOrDefault(eventTypes[0], listOf()))
}