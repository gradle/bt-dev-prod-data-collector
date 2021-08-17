package org.gradle.devprod.collector.enterprise.export

import org.gradle.devprod.collector.persistence.generated.jooq.Tables.BUILD
import org.gradle.devprod.collector.persistence.generated.jooq.Tables.LONG_TEST
import org.jooq.DSLContext
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

const val BULK_SIZE = 10000

@Component
class DatabaseCleanupService(
    private
    val create: DSLContext
) {
    @Async
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    fun cleanupDb() {
        val limitDate = OffsetDateTime.now().minusMonths(2)
        val beforeLimitData = BUILD.BUILD_START.lessOrEqual(limitDate)
        val oldBuildIdsQuery = create.select(BUILD.BUILD_ID)
            .from(BUILD)
            .where(beforeLimitData)
            .orderBy(BUILD.BUILD_START.desc())
            .limit(BULK_SIZE)
        var result: List<String>

        do {
            result = oldBuildIdsQuery.fetch(BUILD.BUILD_ID)

            create.delete(LONG_TEST).where(LONG_TEST.BUILD_ID.`in`(result)).execute()
            create.delete(BUILD).where(BUILD.BUILD_ID.`in`(result)).execute()

            println("Deleted ${result.size} old builds")
        } while (result.size == BULK_SIZE)
    }
}
