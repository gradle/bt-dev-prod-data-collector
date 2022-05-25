package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toSet
import org.gradle.devprod.collector.enterprise.export.model.api.Build
import org.gradle.devprod.collector.enterprise.export.model.api.TaskExecution
import org.gradle.devprod.collector.persistence.generated.jooq.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class GeApiExtractorService(
    private val geApiClient: GeApiClient,
    private val create: DSLContext,
    private val shutdownService: ShutdownService
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun streamToDatabase(): Flow<Unit> =
        geApiClient
            .readBuilds()
            .onEach { println("Received at ${ZonedDateTime.now()}: $it") }
            .map(this::persistToDatabase)
            .onCompletion { failure ->
                failure?.let { logger.error("Failed streaming Gradle enterprise data", it) }
                shutdownService.shutdown()
            }

    private suspend fun persistToDatabase(build: Build) {
        val existing = create.fetchAny(Tables.TASK_TRENDS, Tables.TASK_TRENDS.BUILD_ID.eq(build.id))
        if (existing == null) {
            val performanceData = geApiClient.readBuildCachePerfomanceData(build)
                .toSet()
            performanceData.forEach { performance ->
                println("Build time for ${build.id}: ${performance.buildTime}")
                try {
                    create.transaction { configuration ->
                        performance.taskExecution.forEach { task: TaskExecution ->
                            val ctx = DSL.using(configuration)
                            val taskTrends = ctx.newRecord(Tables.TASK_TRENDS)
                            taskTrends.apply {
                                buildId = build.id
                                projectId = "Unknown"
                                taskPath = task.taskPath
                                buildStart = OffsetDateTime.ofInstant(Instant.ofEpochMilli(build.availableAt), ZoneId.systemDefault())
                                taskDurationMs = task.duration.toInt()
                                status = task.avoidanceOutcome
                            }
                            taskTrends.store()
                        }
                    }
                } catch (e: Exception) {
                    throw IllegalStateException("Error processing $build, performanceData: $performanceData", e)
                }
            }
        }
    }
}