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
    val buildConfigurations = listOf(
        "Gradle_{pipeline}_Check_Stage_QuickFeedbackLinuxOnly_Trigger",
        "Gradle_{pipeline}_Check_Stage_QuickFeedback_Trigger",
        "Gradle_{pipeline}_Check_Stage_ReadyforMerge_Trigger",
        "Gradle_{pipeline}_Check_Stage_ReadyforNightly_Trigger",
        "Gradle_{pipeline}_Check_Stage_ReadyforRelease_Trigger"
    )

    fun loadBuilds(): Sequence<Build> =
        pipelines.flatMap { pipeline ->
            buildConfigurations.map { it.replace("{pipeline}", pipeline) }
        }.asSequence().flatMap { buildConfiguration ->
            teamCityInstance
                .builds()
                .fromConfiguration(BuildConfigurationId(buildConfiguration))
                .includeCanceled()
                .includeFailed()
                .withAllBranches()
                .since(Instant.now().minus(5, ChronoUnit.DAYS))
                .all()
        }
}