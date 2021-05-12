package org.gradle.devprod.collector.teamcity

import org.gradle.devprod.collector.persistence.generated.jooq.Tables
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildStatus
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
        val builds = teamcityClientService.loadBuildsForReadyForNightly().toList().sortedBy { it.finishDateTime }
        builds.forEachIndexed { index, build ->
            val existing = create.fetchAny(Tables.TEAMCITY_BUILD, Tables.TEAMCITY_BUILD.BUILD_ID.eq(build.id.stringId))
            if (existing == null || (build.isPreTestCommitBuild() && existing.preTestedCommitSuccessRate == null)) {
                val branch = build.branch.name
                val status = build.status?.name
                val gitCommitId = build.revisions.first { it.vcsRootInstance.vcsRootId.stringId == "Gradle_Branches_GradlePersonalBranches" }
                val preTestedCommitSuccessRate = calculatePreTestedCommitSuccessRate(index, builds)
                println("Found build for branch $branch with status ${status}, queued at ${build.queuedDateTime}")
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
                    record.preTestedCommitSuccessRate = preTestedCommitSuccessRate

                    record.store()
                }
            }
        }
    }

    private fun Build.isPreTestCommitBuild() = branch.name?.startsWith("pre-test/") == true

    private fun calculatePreTestedCommitSuccessRate(index: Int, builds: List<Build>): Double? {
        val build = builds[index]
        if (!build.isPreTestCommitBuild()) {
            return null
        }
        val baseBranch = build.branch.name!!.substringAfter("pre-test/").substringBefore("/")
        val preTestedCommitBuildsOnSameBaseBranch = builds
            .subList(0, index + 1)
            .filter { it.branch.name?.startsWith("pre-test/$baseBranch/") == true }
            .let {
                if (it.size > 10) it.takeLast(10) else it
            }
        return 1.0 * preTestedCommitBuildsOnSameBaseBranch.filter { it.status == BuildStatus.SUCCESS }.count() / preTestedCommitBuildsOnSameBaseBranch.count()
    }
}