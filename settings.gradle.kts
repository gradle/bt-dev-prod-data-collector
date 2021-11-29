rootProject.name = "dev-prod-data-collector"

include("build-platform")

include("persistence")

include("ge-export")

include("teamcity-export")

include("application")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url =
                uri(
                    "https://packages.jetbrains.team/maven/p/teamcity-rest-client/teamcity-rest-client"
                )
            content { includeModule("org.jetbrains.teamcity", "teamcity-rest-client") }
        }
    }
}

plugins {
    id("com.gradle.enterprise") version "3.7.2"
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin") version "0.7.4"
}
