package org.gradle.devprod.enterprise.export

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class StreamToDatabase(private val exportApiExtractorService: ExportApiExtractorService) {
    @Bean
    fun streamToDatabaseJob(): Job =
        exportApiExtractorService.streamToDatabase().launchIn(CoroutineScope(Dispatchers.IO))
}