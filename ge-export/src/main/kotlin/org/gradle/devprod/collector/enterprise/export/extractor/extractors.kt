package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import java.time.Instant

object BuildStarted : Extractor<Instant>("BuildStarted") {
    override
    fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

object BuildFinished : Extractor<Instant>("BuildFinished") {
    override fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

object RootProjectNames : Extractor<List<String>>("ProjectStructure") {
    override fun extract(events: Iterable<BuildEvent>): List<String> =
        events.filter { it.eventType == "ProjectStructure" }
            .mapNotNull { it.data?.stringProperty("rootProjectName") }
}

object FirstTestTaskStart : Extractor<Pair<String, Instant>?>("TaskStarted") {
    override fun extract(events: Iterable<BuildEvent>): Pair<String, Instant>? {
        val testTasksStarted = events
            .filter { it.data?.stringProperty("className")?.endsWith("Test") ?: false }
            .map {
                val path = it.data?.stringProperty("path")!!
                val startTime = Instant.ofEpochMilli(it.timestamp)
                path to startTime
            }
        return testTasksStarted.minByOrNull { it.second }
    }
}

object Tags : Extractor<Set<String>>("UserTag") {
    override fun extract(events: Iterable<BuildEvent>): Set<String> =
        events.map { it.data?.stringProperty("tag")!! }.toSet()

}

object BuildAgent : Extractor<Agent>("BuildAgent") {
    override fun extract(events: Iterable<BuildEvent>): Agent =
        events.first().data!!.let { Agent(it.stringProperty("localHostname"), it.stringProperty("username")) }
}

data class Agent(val host: String?, val user: String?)

private fun Any.stringProperty(name: String): String? = (this as Map<*, *>)[name] as String?
