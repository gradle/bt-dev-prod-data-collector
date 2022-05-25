package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toSet
import org.gradle.devprod.collector.enterprise.export.model.api.Build
import org.gradle.devprod.collector.persistence.generated.jooq.Tables
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TASK_TRENDS
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
            .filter { it.buildToolType == "gradle" }
            .map(this::persistToDatabase)
            .onCompletion { failure ->
                failure?.let { logger.error("Failed streaming Gradle enterprise data", it) }
                shutdownService.shutdown()
            }

    private suspend fun persistToDatabase(build: Build) {
        val existing = create.fetchAny(Tables.BUILD_TRENDS, Tables.BUILD_TRENDS.BUILD_ID.eq(build.id))
        if (existing == null) {
            val performanceData = geApiClient.readBuildCachePerfomanceData(build)
                .toSet()
            val buildAttributes = geApiClient.readBuildAttributes(build)
                .single()
            performanceData.forEach { performance ->
                println("Build time for ${build.id}: ${performance.buildTime}")
                try {
                    create.transaction { configuration ->
                        val ctx = DSL.using(configuration)
                        val buildTrend = ctx.newRecord(Tables.BUILD_TRENDS)
                        buildTrend.apply {
                            buildId = build.id
                            buildStart = OffsetDateTime.ofInstant(Instant.ofEpochMilli(build.availableAt), ZoneId.systemDefault())
                            projectId = buildAttributes.rootProjectName
                            tags = buildAttributes.tags.toTypedArray()
                        }
                        buildTrend.store()
                        ctx.batch(performance.taskExecution.map { task ->
                            ctx.insertInto(
                                TASK_TRENDS,
                                TASK_TRENDS.BUILD_ID,
                                TASK_TRENDS.TASK_PATH,
                                TASK_TRENDS.TASK_DURATION_MS,
                                TASK_TRENDS.STATUS
                            ).values(build.id, task.taskPath, task.duration.toInt(), task.avoidanceOutcome)
                        }).execute()
                    }
                } catch (e: Exception) {
                    throw IllegalStateException("Error processing $build, performanceData: $performanceData", e)
                }
            }
        }
    }
}