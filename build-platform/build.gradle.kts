plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.7.12"))

    constraints {
        api("org.jetbrains.teamcity:teamcity-rest-client:1.19.0")
    }
}
