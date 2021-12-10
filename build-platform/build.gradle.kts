plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.4"))

    constraints {
        api("org.apache.logging.log4j:log4j-api:2.15.0")
        api("org.apache.logging.log4j:log4j-to-slf4j:2.15.0")
        api("org.flywaydb:flyway-core:7.7.1")
        api("org.jetbrains.teamcity:teamcity-rest-client:1.14.0")
    }
}
