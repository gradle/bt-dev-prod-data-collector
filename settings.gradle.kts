rootProject.name = "dev-prod-data-collector"

include("persistence")
include("ge-export")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
