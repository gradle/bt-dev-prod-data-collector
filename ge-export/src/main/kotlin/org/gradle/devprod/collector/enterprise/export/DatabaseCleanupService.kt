package org.gradle.devprod.collector.enterprise.export

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.BUILD
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.FLAKY_TEST_CLASS
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.LONG_TEST
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.PRECONDITION_TEST
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.TEAMCITY_BUILD
import org.jooq.DSLContext
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

const val BULK_SIZE = 10000

@Component
class DatabaseCleanupService(private val create: DSLContext) {
    final var broken: Boolean = false
        private set

    private fun deleteInTeamCityBuildTable(date: OffsetDateTime) {
        create.delete(TEAMCITY_BUILD).where(TEAMCITY_BUILD.FINISHED.lessOrEqual(date)).execute()
    }

    private fun deleteInBuildTable(date: OffsetDateTime) {
        val beforeLimitData = BUILD.BUILD_START.lessOrEqual(date)
        val oldBuildIdsQuery =
            create
                .select(BUILD.BUILD_ID)
                .from(BUILD)
                .where(beforeLimitData)
                .orderBy(BUILD.BUILD_START.desc())
                .limit(BULK_SIZE)
        var result: List<String>

        do {
            result = oldBuildIdsQuery.fetch(BUILD.BUILD_ID)

            create.delete(LONG_TEST).where(LONG_TEST.BUILD_ID.`in`(result)).execute()
            create.delete(FLAKY_TEST_CLASS).where(FLAKY_TEST_CLASS.BUILD_ID.`in`(result)).execute()
            create.delete(BUILD).where(BUILD.BUILD_ID.`in`(result)).execute()

            println("Deleted ${result.size} old builds in `build` table")
        } while (result.size == BULK_SIZE)
    }

    /**
     * Cleanes up precondition tests, such that only the last `retained_count` builds are kept.
     * Preconditions will be ordered by their builds' start date.
     *
     * The deletion is approximate, as the preconditions are deleted for an entire build_id, and not a (build_id, preconditions) tuple.
     * This means that remaining record counts can be lower than `retained_count`.
     */
    protected fun deleteInPreconditionsTable(retained_count: Int = 1_000_000) {
        val selectQuery = create
            .select(PRECONDITION_TEST.BUILD_ID)
            .from(PRECONDITION_TEST)
            .join(BUILD).on(PRECONDITION_TEST.BUILD_ID.eq(BUILD.BUILD_ID))
            .orderBy(BUILD.BUILD_START.desc())
            .offset(retained_count)

        val deletedCount = create
            .deleteFrom(PRECONDITION_TEST)
                .where(PRECONDITION_TEST.BUILD_ID.`in`(selectQuery))
            .execute()

        println("Deleted $deletedCount old precondition tests in `precondition_test` table")
    }

    @Async
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    fun cleanupDb() {
        try {
            val limitDate = OffsetDateTime.now().minusMonths(3)

            deleteInBuildTable(limitDate)
            deleteInTeamCityBuildTable(limitDate)
            deleteInPreconditionsTable()

            broken = false
        } catch (e: Exception) {
            broken = true
            throw e
        }
    }
}
