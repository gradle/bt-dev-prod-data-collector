package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JooqRepository(private val dslContext: DSLContext) : Repository {
    override fun getBuildById(id: String): TeamCityBuild? {
        val record = dslContext.fetchAny(TEAMCITY_BUILD, TEAMCITY_BUILD.BUILD_ID.eq(id))
        if (record == null) {
            return null
        } else {
            return TeamCityBuild(
                record.buildId,
                record.branch,
                record.status,
                record.gitCommitId,
                record.queued,
                record.dependencyFinished,
                record.started,
                record.finished,
                record.state,
                record.configuration,
                record.statusText,
                record.composite,
                record.buildscanUrls.toList(),
                record.buildHostName,
                record.buildHostType,
            )
        }
    }

    override fun storeBuild(build: TeamCityBuild) {
        val existing = getBuildById(build.id)
        if (existing == null) {
            println(
                "Found build ${build.id} for branch ${build.branch} with status ${build.status}, queued at ${build.queuedDateTime}, buildscan: ${build.buildScanUrls}",
            )
            dslContext.transaction { configuration ->
                val ctx = DSL.using(configuration)
                val record = ctx.newRecord(TEAMCITY_BUILD)
                record.buildId = build.id
                record.configuration = build.buildConfigurationId
                record.queued = build.queuedDateTime
                record.dependencyFinished = build.dependencyFinishedDateTime
                record.started = build.startDateTime
                record.finished = build.finishDateTime
                record.state = build.state.uppercase()
                record.status = build.status?.uppercase()
                record.statusText = build.statusText
                record.branch = build.branch
                record.gitCommitId = build.gitCommitId
                record.composite = build.composite
                record.buildscanUrls = build.buildScanUrls.toTypedArray()
                record.buildHostName = build.buildHostName
                record.buildHostType = build.buildHostType

                record.store()
            }
        }
    }

    override fun latestFinishedBuildTimestamp(projectIdPrefix: String): Instant? {
        val latestFailedBuild = dslContext.select(TEAMCITY_BUILD.FINISHED)
            .from(TEAMCITY_BUILD)
            .where(TEAMCITY_BUILD.CONFIGURATION.startsWith(projectIdPrefix))
            .orderBy(TEAMCITY_BUILD.FINISHED.desc())
            .fetchAny() ?: return null

        return TEAMCITY_BUILD.FINISHED.get(latestFailedBuild)!!.toInstant()
    }
}
