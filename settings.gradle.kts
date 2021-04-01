rootProject.name = "dev-prod-data-collector"

include("build-platform")
include("persistence")
include("ge-export")
include("application")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
