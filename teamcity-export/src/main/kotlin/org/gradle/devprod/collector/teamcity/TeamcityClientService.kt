package org.gradle.devprod.collector.teamcity

import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TeamcityClientService {
    private
    val teamCityInstance: TeamCityInstance = TeamCityInstanceFactory.guestAuth("https://builds.gradle.org")
    private
    val pipelines = listOf("Master", "Release")
    private
    fun readyForNightlyFor(pipeline: String) = BuildConfigurationId("Gradle_${pipeline}_Check_Stage_ReadyforNightly_Trigger")

    fun loadBuildsForReadyForNightly(): Sequence<Build> =
        pipelines.asSequence().flatMap { pipeline ->
            teamCityInstance
                .builds()
                .fromConfiguration(readyForNightlyFor(pipeline))
                .includeCanceled()
                .includeFailed()
                .withAllBranches()
                .since(Instant.now().minus(5, ChronoUnit.DAYS))
                .all()
        }

}