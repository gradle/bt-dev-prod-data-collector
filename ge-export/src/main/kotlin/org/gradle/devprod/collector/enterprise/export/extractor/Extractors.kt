package org.gradle.devprod.collector.enterprise.export.extractor

import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import org.slf4j.LoggerFactory
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

object TaskSummaryExtractor : SingleEventExtractor<TaskSummary>("TaskFinished") {
    override fun extract(events: Iterable<BuildEvent>): TaskSummary {
        return TaskSummary(
            events
                .filter { it.data?.stringProperty("outcome") != null }
                .groupBy { it.data?.stringProperty("outcome") }
                .mapKeys { TaskOutcome.valueOf(it.key!!.uppercase()) }
                .mapValues { it.value.size },
        )
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
                it.data?.stringProperty("outcome")?.uppercase() in listOf("SUCCESS", "FAILED") &&
                    idToClassName[it.data?.longProperty("id")]?.endsWith("Test") == true
            }
            .mapNotNull { it.data?.stringProperty("path") }
            .filter { it.endsWith("Test") }
    }
}

data class TestCase(val taskPath: String, val name: String, val className: String)

object TestSummaryExtractor : Extractor<TestSummary>(listOf("TestStarted", "TestFinished", "TaskStarted")) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): TestSummary {
        val testCaseRegistry: TestCaseRegistry = getTestCaseRegistry(events)
        var totalCount = 0
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        events.getOrDefault(LongTestClassExtractor.eventTypes[1], emptyList()).forEach {
            val testCase = testCaseRegistry.getTestCase(it) ?: return@forEach
            if (testCase.name == testCase.className) {
                return@forEach
            }
            val failed = it.data?.booleanProperty("failed") ?: return@forEach
            val skipped = it.data.booleanProperty("skipped") ?: return@forEach

            when {
                skipped -> skippedCount++
                failed -> failedCount++
                else -> successCount++
            }
            totalCount++
        }
        return TestSummary(totalCount, failedCount, successCount, skippedCount)
    }
}

/**
 * Test case's id is only guaranteed to be unique in the same task,
 * thus we have to use combined key to uniquely identify a test case.
 */
data class TestCaseKey(val testCaseId: Long, val taskId: Long)

class TestCaseRegistry(
    private val testCaseKeyToTestCase: Map<TestCaseKey, TestCase>,
) {
    fun getTestCase(testFinishedEvent: BuildEvent): TestCase? {
        val id = testFinishedEvent.data?.longProperty("id") ?: return null
        val taskId = testFinishedEvent.data.longProperty("task") ?: return null
        return testCaseKeyToTestCase[TestCaseKey(id, taskId)]
    }
}

/**
 * Builds a reusable map from separate build events.
 *
 * As TestFinished and TestStarted events are not connected together, this utility method creates a handy map, which
 * can be used to look up a TestStarted event by its id.
 *
 * This is very handy if a TestStarted and TestFinished event needs to be combined.
 */
private fun getTestCaseRegistry(typeToEvents: Map<String?, List<BuildEvent>>): TestCaseRegistry {
    val idToTaskPath: Map<Long, String> = getIdToTaskPathMap(typeToEvents.getOrDefault("TaskStarted", emptyList()))
    val testCaseKeyToTestCase: MutableMap<TestCaseKey, TestCase> = mutableMapOf()
    typeToEvents.getOrDefault("TestStarted", emptyList()).forEach {
        val id = it.data?.longProperty("id")
        val name = it.data?.stringProperty("name")
        val className = it.data?.stringProperty("className")
        val taskId = it.data?.longProperty("task")
        val taskPath = taskId?.let { idToTaskPath[it] }
        val suite = it.data?.booleanProperty("suite") ?: false

        if (!suite && id != null && name != null && className != null && taskPath != null && name != className) {
            val testCase = TestCase(taskPath, name, className)
            testCaseKeyToTestCase[TestCaseKey(id, taskId)] = testCase
        }
    }
    return TestCaseRegistry(testCaseKeyToTestCase)
}

private fun getIdToTaskPathMap(events: List<BuildEvent>): Map<Long, String> {
    val result: MutableMap<Long, String> = mutableMapOf()
    events.forEach {
        val id = it.data?.longProperty("id")
        val path = it.data?.stringProperty("path")
        if (id != null && path != null) {
            result[id] = path
        }
    }
    return result
}

/**
 * Extract flaky test classes from the build events.
 *
 * Currently, Export API doesn't provide this information directly,
 * so we need to construct a mapping of [testCase -> testResult],
 * and find out the flaky test classes.
 */
object FlakyTestClassExtractor : Extractor<Set<String>>(listOf("TestStarted", "TestFinished", "TaskStarted")) {
    override fun extractFrom(events: Map<String?, List<BuildEvent>>): Set<String> {
        val testCaseRegistry: TestCaseRegistry = getTestCaseRegistry(events)
        val flakyTestClasses = mutableSetOf<String>()
        val testCaseToFailedResult: MutableMap<TestCase, Boolean> = mutableMapOf()
        events.getOrDefault(LongTestClassExtractor.eventTypes[1], emptyList()).forEach {
            val failed = it.data?.booleanProperty("failed") ?: return@forEach
            val skipped = it.data.booleanProperty("skipped")
            val testCase = testCaseRegistry.getTestCase(it) ?: return@forEach
            val existingResult = testCaseToFailedResult[testCase]
            if (existingResult == null) {
                testCaseToFailedResult[testCase] = failed
            } else if (existingResult != failed && skipped != true) {
                flakyTestClasses.add(testCase.className)
            }
        }

        return flakyTestClasses
    }
}

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/gradle/BuildRequestedTasks_1_0.html
object BuildRequestedTasks : SingleEventExtractor<String>("BuildRequestedTasks") {
    override fun extract(events: Iterable<BuildEvent>): String {
        val excludedTasks = events.first().data?.listStringProperty("excluded") ?: emptyList()
        val requestedTasks = events.first().data?.listStringProperty("requested") ?: emptyList()
        return requestedTasks.joinToString(" ") + excludedTasks.joinToString(" ") { "-x $it" }
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
                it.stringProperty("username"),
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

// https://docs.gradle.com/enterprise/event-model-javadoc/com/gradle/scan/eventmodel/gradle/TaskFinished_1_6.html#cachingDisabledReason
object UnexpectedCachingDisableReasonsExtractor : SingleEventExtractor<List<String>>("TaskFinished") {
    override fun extract(events: Iterable<BuildEvent>): List<String> {
        return events
            .mapNotNull { it.data?.stringProperty("cachingDisabledReasonCategory") }
            .filter { it == "OVERLAPPING_OUTPUTS" }
            .distinct()
    }
}

/**
 * Extracts all tests, which are part of
 * `org.gradle.test.predicate.RemotePreconditionProbingTests`
 * or
 * `org.gradle.test.predicate.LocalPreconditionProbingTests` classes
 */
object PreconditionTestsExtractor :
    Extractor<List<PreconditionTest>>(listOf("TestStarted", "TestFinished", "TaskStarted")) {

    private const val PRECONDITION_PATTERN_START = "Preconditions ["
    private const val PRECONDITION_PATTERN_END = "]"

    private val logger = LoggerFactory.getLogger(PreconditionTestsExtractor::class.java)

    override fun extractFrom(events: Map<String?, List<BuildEvent>>): List<PreconditionTest> {
        // Build the lookup map to lookup TestStarted events
        val testCaseRegistry: TestCaseRegistry = getTestCaseRegistry(events)

        return events.getOrDefault("TestFinished", emptyList()).mapNotNull {
            val testCase = testCaseRegistry.getTestCase(it) ?: return@mapNotNull null
            val failed = it.data?.booleanProperty("failed") ?: return@mapNotNull null
            val skipped = it.data.booleanProperty("skipped") ?: return@mapNotNull null

            if (!isPreconditionName(testCase.name)) {
                return@mapNotNull null
            }

            val preconditions: MutableList<String> = mutableListOf()
            try {
                preconditions.addAll(
                    extractPreconditionNames(testCase.name).sorted(),
                )
            } catch (ex: IllegalArgumentException) {
                logger.error("Exception meanwhile processing preconditions: {0}", ex)
                return@mapNotNull null
            }

            PreconditionTest(
                testCase.className,
                testCase.taskPath,
                preconditions,
                if (failed) TestOutcome.FAILED else if (skipped) TestOutcome.SKIPPED else TestOutcome.PASSED,
            )
        }
    }

    private fun isPreconditionName(name: String): Boolean =
        name.startsWith(PRECONDITION_PATTERN_START) && name.endsWith(PRECONDITION_PATTERN_END)

    /**
     * Extracts from the test name the list of preconditions used.
     *
     * Test names should contain the preconditions in a brace enclosed, comma separated format, e.g.:
     * `Precondition [precondition1, precondition2, ...]`
     *
     * See `PreconditionProbingTests.groovy` in the gradle/gradle project to see how the test names are generated.
     *
     * @throws IllegalArgumentException if the test name does not start with 'Precondition ['.
     * @throws IllegalArgumentException if the test name does not end with ']'.
     * @throws IllegalArgumentException if the test name does not contain any preconditions.
     */
    fun extractPreconditionNames(name: String): List<String> {
        if (!name.startsWith(PRECONDITION_PATTERN_START)) {
            throw IllegalArgumentException("Test name '$name' does not start with '$PRECONDITION_PATTERN_START'")
        }
        if (!name.endsWith(PRECONDITION_PATTERN_END)) {
            throw IllegalArgumentException("Test name '$name' does not end with '$PRECONDITION_PATTERN_END'")
        }

        val preconditions = name
            .removeSurrounding(PRECONDITION_PATTERN_START, PRECONDITION_PATTERN_END)
            .split(",")
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }

        if (preconditions.isEmpty()) {
            throw IllegalArgumentException("Test name '$name' doesn't contain any preconditions")
        }

        return preconditions
    }
}

private fun Any.booleanProperty(name: String): Boolean? = (this as Map<*, *>)[name] as Boolean?

private fun Any.listStringProperty(name: String): List<String>? = (this as Map<*, *>)[name] as List<String>?

private fun Any.stringProperty(name: String): String? = (this as Map<*, *>)[name] as String?

private fun Any.anyProperty(name: String): Any? = (this as Map<*, *>)[name]

private fun Any.intProperty(name: String): Int? = (this as Map<*, *>)[name] as Int?

private fun Any.longProperty(name: String): Long? = (this as Map<*, *>)[name] as Long?
