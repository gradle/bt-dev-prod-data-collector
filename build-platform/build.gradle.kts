plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.4"))

    constraints {
        api("org.flywaydb:flyway-core:7.7.1")
        api("org.jetbrains.teamcity:teamcity-rest-client:1.14.0")
        api("com.fasterxml.jackson.core:jackson-core:2.13.0")
        api("com.fasterxml.jackson.core:jackson-databind:2.13.0")
        api("com.fasterxml.jackson.core:jackson-annotations:2.13.0")
    }
}
