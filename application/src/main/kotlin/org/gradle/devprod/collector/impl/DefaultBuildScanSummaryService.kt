package org.gradle.devprod.collector.impl

import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.gradle.devprod.collector.api.BuildScanSummaryService
import org.gradle.devprod.collector.enterprise.export.ExportApiClient
import org.gradle.devprod.collector.enterprise.export.GradleEnterpriseServer
import org.gradle.devprod.collector.enterprise.export.extractor.BuildFailure
import org.gradle.devprod.collector.enterprise.export.extractor.BuildFinished
import org.gradle.devprod.collector.enterprise.export.extractor.BuildRequestedTasks
import org.gradle.devprod.collector.enterprise.export.extractor.BuildStarted
import org.gradle.devprod.collector.enterprise.export.extractor.ExecutedTestTasks
import org.gradle.devprod.collector.enterprise.export.extractor.RootProjectNames
import org.gradle.devprod.collector.enterprise.export.extractor.Tags
import org.gradle.devprod.collector.enterprise.export.model.BuildEvent
import org.gradle.devprod.collector.model.BuildScanOutcome
import org.gradle.devprod.collector.model.BuildScanSummary
import org.gradle.devprod.collector.model.TaskSummary
import org.gradle.devprod.collector.model.TestSummary
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * By convention, token for ge.gradle.org is stored in env variable "GE_GRADLE_ORG_EXPORT_API_TOKEN"
 */
private fun getExportApiTokenFor(geHost: String): String {
    val envName = geHost.replace(".", "_").toUpperCase() + "_EXPORT_API_TOKEN"
    return System.getenv(envName) ?: throw IllegalArgumentException("You must set env variable $envName to access $geHost")
}

@Service
class DefaultBuildScanSummaryService(private val geClients: Map<String, ExportApiClient>) : BuildScanSummaryService {
    @Autowired
    constructor() : this(listOf(
        "ge.gradle.org",
        "e.grdev.net",
        "ge-unstable.grdev.net"
    ).associateWith { ExportApiClient(GradleEnterpriseServer(it, getExportApiTokenFor(it))) })

    override fun getSummary(geServerHost: String, buildScanId: String): BuildScanSummary = runBlocking {
        val client = geClients[geServerHost] ?: throw IllegalArgumentException("Unsupported server: $geServerHost")

        val extractors =
            listOf(
                BuildStarted,
                BuildFinished,
                BuildFailure,
                Tags,
                RootProjectNames,
                BuildRequestedTasks,
                ExecutedTestTasks
            )
        val eventTypes = extractors.flatMap { it.eventTypes }.distinct()
        val events: List<BuildEvent> = client.getEvents(buildScanId, eventTypes)
            .toSet()
            .mapNotNull { it.data() }
            .toList()

        val typeToEvents: Map<String?, List<BuildEvent>> = events.groupBy { it.eventType }

        BuildScanSummary(
            RootProjectNames.extract(events).firstOrNull() ?: "",
            BuildStarted.extract(events),
            BuildFinished.extract(events),
            if (BuildFailure.extract(events)) BuildScanOutcome.FAILURE else BuildScanOutcome.SUCCESS,
            Tags.extractFrom(typeToEvents).toList(),
            BuildRequestedTasks.extractFrom(typeToEvents),
            TaskSummary(0),
            TestSummary(0)
        )
    }
}
