package org.gradle.devprod.collector.enterprise.export.extractor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.devprod.collector.enterprise.export.model.BuildEvent

// The events can be fetched from the following URL:
// curl -H "Authorization: Bearer <TOKEN>" 'https://ge.gradle.org/build-export/v2/build/<buildId>/events?eventTypes=TestStarted,TestFinished'
//
// ---------------------------------------------------------------------------------------------------------------------
// id: 25
// event: BuildEvent
// data: {"timestamp":1652776255296,
//        "type":{"majorVersion":1,"minorVersion":1,"eventType":"TestFinished"},
//        "data":{"task":4391029188895952316,"id":93387128101402608,"failed":false,"skipped":false,"failureId":null,"failure":null}}
//
// id: 26
// ...

fun parse(resourcePath: String): List<BuildEvent> {
    val text = BuildEvent::class.java.getResourceAsStream(resourcePath).reader().readText()
    return text.split("\n\n").mapNotNull(::parseSingleEvent)
}

private val objectMapper = ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerKotlinModule()

private fun parseSingleEvent(eventLines: String): BuildEvent? {
    val json = eventLines.lines().firstOrNull { it.startsWith("data:") }?.substring(5) ?: return null
    return objectMapper.readValue(json, BuildEvent::class.java)
}
