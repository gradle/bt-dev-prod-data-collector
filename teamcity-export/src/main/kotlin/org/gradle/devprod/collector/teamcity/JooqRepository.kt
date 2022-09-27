package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD
import org.jetbrains.teamcity.rest.BuildStatus
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JooqRepository(private val dslContext: DSLContext) : Repository {

    override fun storeBuild(build: TeamCityBuild) {
        val existing = dslContext.fetchAny(TEAMCITY_BUILD, TEAMCITY_BUILD.BUILD_ID.eq(build.id))
        if (existing == null) {
            println(
                "Found build ${build.id} for branch ${build.branch} with status ${build.status}, queued at ${build.queuedDateTime}, buildscan: ${build.buildScanUrls}"
            )
            dslContext.transaction { configuration ->
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

                // FIXME - how to store the new fileds?

                record.store()
            }
        }
    }

    override fun latestFailedBuildTimestamp(buildNamePrefix: String): Instant? {
        val latestFailedBuild = dslContext.select(TEAMCITY_BUILD.FINISHED)
            .from(TEAMCITY_BUILD)
            .where(TEAMCITY_BUILD.COMPOSITE.eq(false))
            .and(TEAMCITY_BUILD.STATUS.notEqual(BuildStatus.SUCCESS.name))
            .and(TEAMCITY_BUILD.CONFIGURATION.contains(buildNamePrefix))
            .orderBy(TEAMCITY_BUILD.FINISHED.desc())
            .fetchAny() ?: return null

        return TEAMCITY_BUILD.FINISHED.get(latestFailedBuild)!!.toInstant()
    }
}
