rootProject.name = "dev-prod-data-collector"

include("build-platform")
include("persistence")
include("ge-export")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    resolutionStrategy.eachPlugin {
        if (requested.id.id.startsWith("org.jetbrains.kotlin"))  {
            if (requested.version == null) {
                useVersion("1.4.31")
            }
        } else if (requested.id.id == "org.springframework.boot")  {
            if (requested.version == null) {
                useVersion("2.4.4")
            }
        }
    }
}
