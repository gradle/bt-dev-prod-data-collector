package org.gradle.devprod.collector.enterprise.export

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gradle.enterprise")
data class GradleEnterpriseServer(val host: String, val apiToken: String) {
    val url = "https://$host"
}
