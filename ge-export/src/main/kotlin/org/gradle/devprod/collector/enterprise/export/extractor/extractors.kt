package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import java.time.Duration
import java.time.Instant

// Maven builds don't seem to have a build started event
object IsGradleBuild : SingleEventExtractor<Boolean>("BuildStarted") {
    override
    fun extract(events: Iterable<BuildEvent>): Boolean =
        events.any()
}

object BuildStarted : SingleEventExtractor<Instant>("BuildStarted") {
    override
    fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

/**
 * This is how we extract the test class running time:
 *
 * 1. Find out a `TestStarted` event with `data.suite=true` and `data.name=data.className, not null`, extract its `id`.
 * 2. Find out a `TestFinished` event with that `id`.
 * 3. Subtract to get the time (in ms).
 */
object LongTestClassExtractor : Extractor<Map<String, Duration>>(listOf("TestStarted", "TestFinished")) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): Map<String, Duration> {
        val testIdToClassName: MutableMap<Long, String> = mutableMapOf()
        val testIdToStartTime: MutableMap<Long, Instant> = mutableMapOf()
        events.getOrDefault(eventTypes[0], emptyList())
            .forEach {
                val id = it.data?.longProperty("id")
                val name = it.data?.stringProperty("name")
                val className = it.data?.stringProperty("className")
                val suite = it.data?.booleanProperty("suite")
                if (suite == true && id != null && className != null && name == className) {
                    val startTime = Instant.ofEpochMilli(it.timestamp)
                    testIdToClassName[id] = className
                    testIdToStartTime[id] = startTime
                }
            }
        return events.getOrDefault(eventTypes[1], emptyList()).filter {
            testIdToClassName.containsKey(it.data?.longProperty("id"))
        }.map {
            val id = it.data?.longProperty("id")!!
            val endTime = Instant.ofEpochMilli(it.timestamp)
            testIdToClassName.getValue(id) to Duration.between(testIdToStartTime.getValue(id), endTime)
        }.groupBy({ it.first }) {
            it.second
        }.mapValues { it: Map.Entry<String, List<Duration>> ->
            it.value.maxOrNull()!!
        }
    }
}

object BuildFinished : SingleEventExtractor<Instant>("BuildFinished") {
    override fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

object BuildFailure : SingleEventExtractor<Boolean>("BuildFinished") {
    override fun extract(events: Iterable<BuildEvent>): Boolean =
        events.first().data?.anyProperty("failureId") != null
}

object RootProjectNames : SingleEventExtractor<List<String>>("ProjectStructure") {
    override fun extract(events: Iterable<BuildEvent>): List<String> =
        events.filter { it.eventType == "ProjectStructure" }
            .mapNotNull { it.data?.stringProperty("rootProjectName") }
}

object FirstTestTaskStart : SingleEventExtractor<Pair<String, Instant>?>("TaskStarted") {
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

object Tags : SingleEventExtractor<Set<String>>("UserTag") {
    override fun extract(events: Iterable<BuildEvent>): Set<String> =
        events.map { it.data?.stringProperty("tag")!! }.toSet()

}

object CustomValues : SingleEventExtractor<List<Pair<String, String>>>("UserNamedValue") {
    override fun extract(events: Iterable<BuildEvent>): List<Pair<String, String>> =
        events.map { it.data?.stringProperty("key")!! to it.data.stringProperty("value")!! }.toList()
}

object BuildAgent : SingleEventExtractor<Agent>("BuildAgent") {
    override fun extract(events: Iterable<BuildEvent>): Agent =
        events.first().data!!.let {
            Agent(it.stringProperty("localHostname")
                ?: it.stringProperty("publicHostname"), it.stringProperty("username")
            )
        }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/DaemonState_1_1.html
object DaemonState : SingleEventExtractor<Int?>("DaemonState") {
    override fun extract(events: Iterable<BuildEvent>): Int? {
        return events.firstOrNull()?.data?.intProperty("buildNumber")
    }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/DaemonUnhealthy_1_0.html
object DaemonUnhealthy : SingleEventExtractor<String?>("DaemonUnhealthy") {
    override fun extract(events: Iterable<BuildEvent>): String? {
        return events.firstOrNull()?.data?.stringProperty("reason")
    }
}

data class Agent(val host: String?, val user: String?)

private fun Any.booleanProperty(name: String): Boolean? = (this as Map<*, *>)[name] as Boolean?
private fun Any.stringProperty(name: String): String? = (this as Map<*, *>)[name] as String?
private fun Any.anyProperty(name: String): Any? = (this as Map<*, *>)[name]
private fun Any.intProperty(name: String): Int? = (this as Map<*, *>)[name] as Int?
private fun Any.longProperty(name: String): Long? = (this as Map<*, *>)[name] as Long?
