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
    fun loadBuilds() {
        println("Loading data from Teamcity")
        val builds = teamcityClientService.loadBuilds()
        builds.forEach { build ->
                val existing = create.fetchAny(Tables.TEAMCITY_BUILD, Tables.TEAMCITY_BUILD.BUILD_ID.eq(build.id.stringId))
                if (existing == null) {
                    val branch = build.branch.name
                    val status = build.status?.name
                    val gitCommitId = build.revisions.first { it.vcsRootInstance.vcsRootId.stringId == "Gradle_Branches_GradlePersonalBranches" }
                    println("Found build for branch ${branch} with status ${status}, queued at ${build.queuedDateTime}")
                    create.transaction { configuration ->
                        val ctx = DSL.using(configuration)
                        val record = ctx.newRecord(Tables.TEAMCITY_BUILD)
                        record.buildId = build.id.stringId
                        record.configuration = build.buildConfigurationId.stringId
                        record.queued = build.queuedDateTime.toOffsetDateTime()
                        record.started = build.startDateTime?.toOffsetDateTime()
                        record.finished = build.finishDateTime?.toOffsetDateTime()
                        record.state = build.state.name
                        record.status = status
                        record.statusText = build.statusText
                        record.branch = branch
                        record.gitCommitId = gitCommitId.version

                        record.store()
                    }
                }
            }
    }
}