package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import java.time.Duration
import java.time.Instant

// Maven builds don't seem to have a build started event
object IsGradleBuild : Extractor<Boolean>("BuildStarted") {
    override
    fun extract(events: Iterable<BuildEvent>): Boolean =
        events.any()
}

object BuildStarted : Extractor<Instant>("BuildStarted") {
    override
    fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

data class TestIdAndClassName(
    val id: Long,
    val className: String
)

object TestStarted : Extractor<Map<TestIdAndClassName, Instant>>("TestStarted") {
    override fun extract(events: Iterable<BuildEvent>): Map<TestIdAndClassName, Instant> =
        events.flatMap {
            val id = it.data?.longProperty("id")
            val className = it.data?.stringProperty("className")
            val startTime = Instant.ofEpochMilli(it.timestamp)
            if (id == null || className == null)
                emptyList()
            else
                listOf(TestIdAndClassName(id, className) to startTime)
        }.toMap()
}

class TestFinished(
    private val testClassStartTimes: Map<TestIdAndClassName, Instant>
) : Extractor<Map<String, Duration>>("TestFinished") {
    private val testIdToClassName: Map<Long, String> = testClassStartTimes.keys.associate { it.id to it.className }
    private val testIdToStartTime: Map<Long, Instant> = testClassStartTimes.map { it.key.id to it.value }.toMap()
    override fun extract(events: Iterable<BuildEvent>): Map<String, Duration> = events.filter {
        testIdToClassName.containsKey(it.data?.longProperty("id"))
    }.associate {
        val id = it.data?.longProperty("id")!!
        val endTime = Instant.ofEpochMilli(it.timestamp)

        testIdToClassName.getValue(id) to Duration.between(testIdToStartTime.getValue(id), endTime)
    }
}

object BuildFinished : Extractor<Instant>("BuildFinished") {
    override fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

object BuildFailure : Extractor<Boolean>("BuildFinished") {
    override fun extract(events: Iterable<BuildEvent>): Boolean =
        events.first().data?.anyProperty("failureId") != null
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

object CustomValues : Extractor<List<Pair<String, String>>>("UserNamedValue") {
    override fun extract(events: Iterable<BuildEvent>): List<Pair<String, String>> =
        events.map { it.data?.stringProperty("key")!! to it.data.stringProperty("value")!! }.toList()
}

object BuildAgent : Extractor<Agent>("BuildAgent") {
    override fun extract(events: Iterable<BuildEvent>): Agent =
        events.first().data!!.let {
            Agent(it.stringProperty("localHostname")
                ?: it.stringProperty("publicHostname"), it.stringProperty("username"))
        }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/DaemonState_1_1.html
object DaemonState : Extractor<Int?>("DaemonState") {
    override fun extract(events: Iterable<BuildEvent>): Int? {
        return events.firstOrNull()?.data?.intProperty("buildNumber")
    }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/DaemonUnhealthy_1_0.html
object DaemonUnhealthy : Extractor<String?>("DaemonUnhealthy") {
    override fun extract(events: Iterable<BuildEvent>): String? {
        return events.firstOrNull()?.data?.stringProperty("reason")
    }
}

data class Agent(val host: String?, val user: String?)

private fun Any.stringProperty(name: String): String? = (this as Map<*, *>)[name] as String?
private fun Any.anyProperty(name: String): Any? = (this as Map<*, *>)[name]
private fun Any.intProperty(name: String): Int? = (this as Map<*, *>)[name] as Int?
private fun Any.longProperty(name: String): Long? = (this as Map<*, *>)[name] as Long?
