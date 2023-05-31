plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.1.0"))

    constraints {
        api("org.jetbrains.teamcity:teamcity-rest-client:1.18.0")
    }
}
