package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import org.springframework.context.annotation.Bean

// @Component
class StreamToDatabase(private val exportApiExtractorService: ExportApiExtractorService) {
    @Bean
    fun streamToDatabaseJob(): Job =
        exportApiExtractorService.streamToDatabase().launchIn(CoroutineScope(Dispatchers.IO))
}
