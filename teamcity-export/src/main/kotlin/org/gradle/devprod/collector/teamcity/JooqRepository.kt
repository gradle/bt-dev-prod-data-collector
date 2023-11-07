package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.BUILD
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JooqRepository(private val dslContext: DSLContext) : Repository {
    override fun getBuildScanTagsById(buildScanId: String): List<String> {
        return dslContext.fetchAny(BUILD, BUILD.BUILD_ID.eq(buildScanId))?.tags?.toList() ?: emptyList()
    }

    override fun getBuildById(id: String): TeamCityBuild? {
        val record = dslContext.fetchAny(TEAMCITY_BUILD, TEAMCITY_BUILD.BUILD_ID.eq(id))
        if (record == null) {
            return null
        } else {
            return TeamCityBuild(
                id = record.buildId,
                branch = record.branch,
                status = record.status,
                gitCommitId = record.gitCommitId,
                queuedDateTime = record.queued,
                dependencyFinishedDateTime = record.dependencyFinished,
                startDateTime = record.started,
                finishDateTime = record.finished,
                state = record.state,
                buildConfigurationId = record.configuration,
                statusText = record.statusText,
                composite = record.composite,
                buildScanUrls = record.buildscanUrls.toList(),
                buildHostName = record.buildHostName,
                buildHostType = record.buildHostType,
                hasRetriedBuild = record.hasRetriedBuild,
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
                record.hasRetriedBuild = build.hasRetriedBuild

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
