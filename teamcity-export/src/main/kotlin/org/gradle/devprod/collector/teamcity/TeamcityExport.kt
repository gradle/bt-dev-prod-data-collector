package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TeamcityExport(
    private
    val create: DSLContext,
    private
    val teamcityClientService: TeamcityClientService
) {
    @Async
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun loadTriggerBuilds() {
        println("Loading trigger builds from Teamcity")
        teamcityClientService.loadTriggerBuilds().store()
    }

    private
    fun Sequence<TeamCityBuild>.store() {
        forEach { build ->
            val existing = create.fetchAny(Tables.TEAMCITY_BUILD, Tables.TEAMCITY_BUILD.BUILD_ID.eq(build.id))
            if (existing == null) {
                println("Found build for branch ${build.branch} with status ${build.status}, queued at ${build.queuedDateTime}")
                create.transaction { configuration ->
                    val ctx = DSL.using(configuration)
                    val record = ctx.newRecord(Tables.TEAMCITY_BUILD)
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
                    record.buildscanUrls = build.buildScanUrls.toTypedArray()

                    record.store()
                }
            }
        }
    }

    @Async
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    fun loadFailedBuilds() {
        teamcityClientService.loadFailedBuilds().store()
    }
}
