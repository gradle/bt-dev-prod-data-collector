package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toSet
import org.gradle.devprod.collector.enterprise.export.extractor.BuildAgent
import org.gradle.devprod.collector.enterprise.export.extractor.BuildCacheLoadFailure
import org.gradle.devprod.collector.enterprise.export.extractor.BuildCacheStoreFailure
import org.gradle.devprod.collector.enterprise.export.extractor.BuildFailure
import org.gradle.devprod.collector.enterprise.export.extractor.BuildFinished
import org.gradle.devprod.collector.enterprise.export.extractor.BuildStarted
import org.gradle.devprod.collector.enterprise.export.extractor.CustomValues
import org.gradle.devprod.collector.enterprise.export.extractor.DaemonState
import org.gradle.devprod.collector.enterprise.export.extractor.DaemonUnhealthy
import org.gradle.devprod.collector.enterprise.export.extractor.ExecutedTestTasks
import org.gradle.devprod.collector.enterprise.export.extractor.FirstTestTaskStart
import org.gradle.devprod.collector.enterprise.export.extractor.FlakyTestClassExtractor
import org.gradle.devprod.collector.enterprise.export.extractor.LongTestClassExtractor
import org.gradle.devprod.collector.enterprise.export.extractor.PreconditionTestsExtractor
import org.gradle.devprod.collector.enterprise.export.extractor.RootProjectNames
import org.gradle.devprod.collector.enterprise.export.extractor.Tags
import org.gradle.devprod.collector.enterprise.export.extractor.UnexpectedCachingDisableReasonsExtractor
import org.gradle.devprod.collector.enterprise.export.model.Build
import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import org.gradle.devprod.collector.persistence.generated.jooq.Tables
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.FLAKY_TEST_CLASS
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.LONG_TEST
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.PRECONDITION_TEST
import org.gradle.devprod.collector.persistence.generated.jooq.udt.records.KeyValueRecord
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class ExportApiExtractorService(
    private val exportApiClient: ExportApiClient,
    private val create: DSLContext,
    private val shutdownService: ShutdownService,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun streamToDatabase(): Flow<Unit> =
        exportApiClient
            .createEventStream()
            .map { it.data() }
            .filterNotNull()
            .onEach { println("Received at ${ZonedDateTime.now()}: $it") }
            .filter { it.toolType == "gradle" }
            .map(this::persistToDatabase)
            .onCompletion { failure ->
                failure?.let { logger.error("Failed streaming Gradle enterprise data", it) }
                shutdownService.shutdown()
            }

    private suspend fun persistToDatabase(build: Build) {
        val existing = create.fetchAny(Tables.BUILD, Tables.BUILD.BUILD_ID.eq(build.buildId))
        if (existing == null) {
            val extractors =
                listOf(
                    BuildStarted,
                    BuildFinished,
                    BuildFailure,
                    BuildCacheLoadFailure,
                    BuildCacheStoreFailure,
                    LongTestClassExtractor,
                    FirstTestTaskStart,
                    Tags,
                    CustomValues,
                    RootProjectNames,
                    BuildAgent,
                    DaemonState,
                    DaemonUnhealthy,
                    ExecutedTestTasks,
                    UnexpectedCachingDisableReasonsExtractor,
                    PreconditionTestsExtractor,
                )
            val events: Map<String?, List<BuildEvent>> =
                getEventsByBuild(build, extractors.flatMap { it.eventTypes }.distinct())

            try {
                create.transaction { configuration ->
                    val ctx = DSL.using(configuration)
                    ctx.insertIntoBuildTable(build, events)
                    ctx.insertIntoLongRunningTestTable(build, events)
                    ctx.insertIntoFlakyTestClassTable(build, events)
                    ctx.insertIntoPreconditionTestsTable(build, events)
                }
            } catch (e: Exception) {
                throw IllegalStateException("Error processing $build, events: $events", e)
            }
        }
    }

    private suspend fun getEventsByBuild(
        buildId: Build,
        eventTypes: List<String>,
        retries: Int = 3
    ): Map<String?, List<BuildEvent>> {
        var retryVar = retries
        var lastException: Exception? = null
        while (retryVar-- > 0) {
            try {
                return exportApiClient
                    .getEvents(buildId, eventTypes)
                    .toSet()
                    .mapNotNull { it.data() }
                    .toList()
                    .groupBy(BuildEvent::eventType)
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw lastException!!
    }

    private fun DSLContext.insertIntoBuildTable(build: Build, events: Map<String?, List<BuildEvent>>) {
        val buildStarted = BuildStarted.extractFrom(events)
        val buildFinished = BuildFinished.extractFrom(events)
        val buildTime = Duration.between(buildStarted, buildFinished)
        val buildFailed = BuildFailure.extractFrom(events)
        val rootProjectName =
            RootProjectNames.extractFrom(events).firstOrNull { !it.startsWith("build-logic") }
        val firstTestTaskStart = FirstTestTaskStart.extractFrom(events)
        val timeToFirstTestTask =
            firstTestTaskStart?.let { Duration.between(buildStarted, it.second) }
        val agent = BuildAgent.extractFrom(events)
        val tags = Tags.extractFrom(events)
        val customValues = CustomValues.extractFrom(events)
        val daemonBuildNumber = DaemonState.extractFrom(events)
        val daemonUnhealthyReason = DaemonUnhealthy.extractFrom(events)
        val unexpectedCachingDisableReasons = UnexpectedCachingDisableReasonsExtractor.extractFrom(events)
        println(
            "Duration of build ${build.buildId} for $rootProjectName is ${buildTime.format()}, first test task started after ${timeToFirstTestTask?.format()}",
        )
        val buildCacheLoadFailure = BuildCacheLoadFailure.extractFrom(events)
        val buildCacheStoreFailure = BuildCacheStoreFailure.extractFrom(events)
        val executedTestTasks = ExecutedTestTasks.extractFrom(events)
        val record = newRecord(Tables.BUILD)
        record.buildId = build.buildId
        record.gradleVersion = build.toolVersion
        record.buildStart = OffsetDateTime.ofInstant(buildStarted, ZoneId.systemDefault())
        record.buildFinish = OffsetDateTime.ofInstant(buildFinished, ZoneId.systemDefault())
        record.successful = !buildFailed
        record.timeToFirstTestTask = timeToFirstTestTask?.toMillis()
        record.pathToFirstTestTask = firstTestTaskStart?.first
        record.rootProject = rootProjectName
        record.username = agent.user
        record.host = agent.host
        record.daemonAge = daemonBuildNumber
        record.daemonUnhealthyReason = daemonUnhealthyReason
        record.tags = tags.toTypedArray()
        record.customValues = customValues.map { KeyValueRecord(it.first, it.second) }.toTypedArray()
        record.buildCacheLoadFailure = buildCacheLoadFailure
        record.buildCacheStoreFailure = buildCacheStoreFailure
        record.executedTestTasks = executedTestTasks.toTypedArray()
        record.unexpectedCachingDisabledReasons = unexpectedCachingDisableReasons.toTypedArray()
        record.store()
    }

    private fun DSLContext.insertIntoLongRunningTestTable(build: Build, events: Map<String?, List<BuildEvent>>) {
        val longRunningTestClasses: Map<String, Duration> = LongTestClassExtractor.extractFrom(events)
        if (longRunningTestClasses.isNotEmpty()) {
            batch(
                *longRunningTestClasses
                    .map {
                        insertInto(
                            LONG_TEST,
                            LONG_TEST.BUILD_ID,
                            LONG_TEST.CLASS_NAME,
                            LONG_TEST.DURATION_MS,
                        ).values(build.buildId, it.key, it.value.toMillis())
                    }
                    .toTypedArray(),
            ).execute()
        }
    }

    private fun DSLContext.insertIntoFlakyTestClassTable(build: Build, events: Map<String?, List<BuildEvent>>) {
        val flakyTestClasses = FlakyTestClassExtractor.extractFrom(events)
        if (flakyTestClasses.isNotEmpty()) {
            batch(
                *flakyTestClasses
                    .map {
                        insertInto(
                            FLAKY_TEST_CLASS,
                            FLAKY_TEST_CLASS.BUILD_ID,
                            FLAKY_TEST_CLASS.FLAKY_TEST_FQCN,
                        ).values(build.buildId, it)
                    }
                    .toTypedArray(),
            ).execute()
        }
    }

    private fun DSLContext.insertIntoPreconditionTestsTable(build: Build, events: Map<String?, List<BuildEvent>>) {
        val preconditionTests = PreconditionTestsExtractor.extractFrom(events)
        if (preconditionTests.isNotEmpty()) {
            batch(
                *preconditionTests
                        .map {
                            insertInto(
                                PRECONDITION_TEST,
                                PRECONDITION_TEST.BUILD_ID,
                                PRECONDITION_TEST.CLASS_NAME,
                                PRECONDITION_TEST.PRECONDITIONS,
                                PRECONDITION_TEST.SKIPPED,
                                PRECONDITION_TEST.FAILED
                            ).values(
                                build.buildId,
                                it.className,
                                it.preconditions.toTypedArray(),
                                it.skipped,
                                it.failed
                            )
                        }
                        .toTypedArray()
            ).execute()
        }
    }
}

private fun Duration.format() = "${toMinutes()}:${String.format("%02d", toSecondsPart())} min"
