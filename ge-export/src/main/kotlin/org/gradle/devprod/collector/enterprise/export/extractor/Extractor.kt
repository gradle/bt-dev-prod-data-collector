package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent

abstract class Extractor<T>(val eventType: String) {
    abstract fun extract(events: Iterable<BuildEvent>): T

    fun extractFrom(events: Map<String?, List<BuildEvent>>): T =
        extract(events.getOrDefault(eventType, listOf()))
}