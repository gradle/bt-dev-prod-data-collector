package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import java.time.Duration
import java.time.Instant

object BuildStarted : SingleEventExtractor<Instant>("BuildStarted") {
    override fun extract(events: Iterable<BuildEvent>): Instant =
        Instant.ofEpochMilli(events.first().timestamp)
}

/**
 * This is how we extract the test class running time:
 *
 * 1. Find out a `TestStarted` event with `data.suite=true` and `data.name=data.className, not
 * null`, extract its `id`.
 * 2. Find out a `TestFinished` event with that `id`.
 * 3. Subtract to get the time (in ms).
 */
object LongTestClassExtractor :
    Extractor<Map<String, Duration>>(listOf("TestStarted", "TestFinished")) {
    // We only care long running test classes
    private val longTestThreshold: Duration = Duration.ofSeconds(60)

    override fun extractFrom(events: Map<String?, List<BuildEvent>>): Map<String, Duration> {
        val testIdToClassName: MutableMap<Long, String> = mutableMapOf()
        val testIdToStartTime: MutableMap<Long, Instant> = mutableMapOf()
        events.getOrDefault(eventTypes[0], emptyList()).forEach {
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
        return events
            .getOrDefault(eventTypes[1], emptyList())
            .filter { testIdToClassName.containsKey(it.data?.longProperty("id")) }
            .map {
                val id = it.data?.longProperty("id")!!
                val endTime = Instant.ofEpochMilli(it.timestamp)
                testIdToClassName.getValue(id) to
                    Duration.between(testIdToStartTime.getValue(id), endTime)
            }
            .groupBy({ it.first }) { it.second }
            .mapValues { it.value.maxOrNull()!! }
            .filterValues { it > longTestThreshold }
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

object BuildCacheLoadFailure : SingleEventExtractor<Boolean>("BuildCacheRemoteLoadFinished") {
    override fun extract(events: Iterable<BuildEvent>): Boolean =
        events.any { it.data?.anyProperty("failureId") != null }
}

object BuildCacheStoreFailure : SingleEventExtractor<Boolean>("BuildCacheRemoteStoreFinished") {
    override fun extract(events: Iterable<BuildEvent>): Boolean =
        events.any { it.data?.anyProperty("failureId") != null }
}

object RootProjectNames : SingleEventExtractor<List<String>>("ProjectStructure") {
    override fun extract(events: Iterable<BuildEvent>): List<String> =
        events.filter { it.eventType == "ProjectStructure" }.mapNotNull {
            it.data?.stringProperty("rootProjectName")
        }
}

object FirstTestTaskStart : SingleEventExtractor<Pair<String, Instant>?>("TaskStarted") {
    override fun extract(events: Iterable<BuildEvent>): Pair<String, Instant>? {
        val testTasksStarted =
            events.filter { it.data?.stringProperty("className")?.endsWith("Test") ?: false }.map {
                val path = it.data?.stringProperty("path")!!
                val startTime = Instant.ofEpochMilli(it.timestamp)
                path to startTime
            }
        return testTasksStarted.minByOrNull { it.second }
    }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/gradle/TaskStarted_1_0.html
// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/gradle/TaskFinished_1_0.html
object ExecutedTestTasks : Extractor<List<String>>(listOf("TestStarted", "TestFinished")) {
    // Find the task whose name ends with "Test" and className ends with "Test"
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): List<String> {
        val idToClassName =
            events
                .getOrDefault(LongTestClassExtractor.eventTypes[0], emptyList())
                .map { it.data?.longProperty("id") to it.data?.stringProperty("className") }
                .filter { it.first != null && it.second != null }
                .toMap()

        return events
            .getOrDefault(LongTestClassExtractor.eventTypes[1], emptyList())
            .filter {
                it.data?.stringProperty("outcome")?.toUpperCase() in listOf("SUCCESS", "FAILED") &&
                    idToClassName[it.data?.longProperty("id")]?.endsWith("Test") == true
            }
            .mapNotNull { it.data?.stringProperty("path") }
            .filter { it.endsWith("Test") }
    }
}

data class TestCase(val name: String, val className: String)

/**
 * Extract flaky test classes from the build events.
 *
 * Currently, Export API doesn't provide this information directly,
 * so we need to construct a mapping of [testCase -> testResult],
 * and find out the flaky test classes.
 */
object FlakyTestClassExtractor : Extractor<Set<String>>(listOf("TestStarted", "TestFinished")) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): Set<String> {
        val testIdToTestCase: MutableMap<Long, TestCase> = mutableMapOf()
        events.getOrDefault(LongTestClassExtractor.eventTypes[0], emptyList()).forEach {
            val id = it.data?.longProperty("id")
            val name = it.data?.stringProperty("name")
            val className = it.data?.stringProperty("className")

            if (id != null && name != null && className != null) {
                val testCase = TestCase(name, className)
                testIdToTestCase[id] = testCase
            }
        }
        val flakyTestClasses = mutableSetOf<String>()
        val testCaseToFailedResult: MutableMap<TestCase, Boolean> = mutableMapOf()
        events.getOrDefault(LongTestClassExtractor.eventTypes[1], emptyList()).forEach {
            val id = it.data?.longProperty("id")
            val failed = it.data?.booleanProperty("failed") ?: return@forEach
            val testCase = testIdToTestCase[id] ?: return@forEach
            val existingResult = testCaseToFailedResult[testCase]
            if (existingResult == null) {
                testCaseToFailedResult[testCase] = failed
            } else if (existingResult != failed) {
                flakyTestClasses.add(testCase.className)
            }
        }

        return flakyTestClasses
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
            Agent(
                it.stringProperty("localHostname") ?: it.stringProperty("publicHostname"),
                it.stringProperty("username")
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
