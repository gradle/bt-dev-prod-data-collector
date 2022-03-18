package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD_QUEUE_LENGTH
import org.jetbrains.teamcity.rest.BuildStatus
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Component
class TeamcityExport(
    private val create: DSLContext,
    private val teamcityClientService: TeamcityClientService
) {
    @Async
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun loadTriggerBuilds() {
        println("Loading trigger builds from Teamcity")
        teamcityClientService.loadTriggerBuilds().store()
    }

    @Async
    @Scheduled(cron = "* * * * *", fixedDelay = 60 * 1000)
    fun monitorBuildQueueLength() {
        println("Loading build queue size from Teamcity")
        val buildQueueSize = teamcityClientService.loadCurrentBuildQueueSize() ?: return
        create.transaction { configuration ->
            val ctx = DSL.using(configuration)
            val record = ctx.newRecord(TEAMCITY_BUILD_QUEUE_LENGTH)
            record.length = buildQueueSize
            record.time = ZonedDateTime.now().toOffsetDateTime()

            record.store()
        }
    }

    private fun Sequence<TeamCityBuild>.store() {
        forEach { build ->
            val existing = create.fetchAny(TEAMCITY_BUILD, TEAMCITY_BUILD.BUILD_ID.eq(build.id))
            if (existing == null) {
                println(
                    "Found build ${build.id} for branch ${build.branch} with status ${build.status}, queued at ${build.queuedDateTime}, buildscan: ${build.buildScanUrls}"
                )
                create.transaction { configuration ->
                    val ctx = DSL.using(configuration)
                    val record = ctx.newRecord(TEAMCITY_BUILD)
                    record.buildId = build.id
                    record.configuration = build.buildConfigurationId
                    record.queued = build.queuedDateTime
                    record.started = build.startDateTime
                    record.finished = build.finishDateTime
                    record.state = build.state
                    record.status = build.status
                    record.statusText = build.statusText
                    record.branch = build.branch
                    record.gitCommitId = build.gitCommitId
                    record.composite = build.composite
                    record.buildscanUrls = build.buildScanUrls.toTypedArray()

                    record.store()
                }
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun loadFailedBuilds() {
        println("Loading failed builds from Teamcity")
        val latestFailedBuild =
            create
                .select(TEAMCITY_BUILD.FINISHED)
                .from(TEAMCITY_BUILD)
                .where(
                    TEAMCITY_BUILD
                        .COMPOSITE
                        .eq(false)
                        .and(TEAMCITY_BUILD.STATUS.notEqual(BuildStatus.SUCCESS.name))
                )
                .orderBy(TEAMCITY_BUILD.FINISHED.desc())
                .fetchAny()

        val since =
            if (latestFailedBuild == null) Instant.now().minus(5, ChronoUnit.DAYS)
            else TEAMCITY_BUILD.FINISHED.get(latestFailedBuild)!!.toInstant().minus(1, ChronoUnit.DAYS)

        teamcityClientService.loadFailedBuilds(since).store()
    }
}
