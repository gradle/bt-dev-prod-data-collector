package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent

abstract class Extractor<T>(val eventTypes: List<String>) {
    abstract fun extractFrom(events: Map<String?, List<BuildEvent>>): T
}

abstract class SingleEventExtractor<T>(eventType: String) : Extractor<T>(listOf(eventType)) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): T =
        extract(events.getOrDefault(eventTypes[0], listOf()))

    abstract fun extract(events: Iterable<BuildEvent>): T
}