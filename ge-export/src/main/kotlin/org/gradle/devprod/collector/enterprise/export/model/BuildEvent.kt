package org.gradle.devprod.collector.enterprise.export.model

data class BuildEvent(val timestamp: Long, val type: EventType?, val data: Any?) {
    val eventType: String?
        get() = type?.eventType
}

data class EventType(val majorVersion: Int, val minorVersion: Int, val eventType: String)
