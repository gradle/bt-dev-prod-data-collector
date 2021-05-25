package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent

abstract class Extractor<T>(val eventTypes: List<String>) {
    constructor(eventType: String) : this(listOf(eventType))

    abstract fun extract(events: Iterable<BuildEvent>): T

    open fun extractFrom(events: Map<String?, List<BuildEvent>>): T {
        return extract(eventTypes.flatMap {
            events.getOrDefault(it, listOf())
        })
    }
}