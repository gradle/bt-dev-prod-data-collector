package org.gradle.devprod.collector.enterprise.export

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class StreamToDatabase(private val geApiExtractorService: GeApiExtractorService) {
    @Bean
    fun streamToDatabaseJob(): Job =
        geApiExtractorService.streamToDatabase().launchIn(CoroutineScope(Dispatchers.IO))
}
