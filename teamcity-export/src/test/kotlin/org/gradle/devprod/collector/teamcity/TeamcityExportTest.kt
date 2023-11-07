package org.gradle.devprod.collector.teamcity

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TeamcityExportTest {

    @Test
    @Disabled("This is a manual test that requires a live tc token")
    fun `can download GE builds`() {

        val loggingRepo = object: Repository {
            override fun getBuildScanTagsById(buildScanId: String): List<String> {
                TODO("Not yet implemented")
            }

            override fun getBuildById(id: String): TeamCityBuild? {
                TODO("Not yet implemented")
            }

            override fun storeBuild(build: TeamCityBuild) {
                System.err.println(build)
            }

            override fun latestFinishedBuildTimestamp(projectIdPrefix: String): Instant? {
                return Instant.now().minus(1, ChronoUnit.HOURS)
            }

        }
        val objectMapper = ObjectMapper()
        val tcService = TeamcityClientService(teamCityApiToken = System.getenv("TC_TOKEN"), objectMapper, loggingRepo)
        val exporter = TeamcityExport(loggingRepo, tcService)

        exporter.loadAllGeBuilds()
    }

}
