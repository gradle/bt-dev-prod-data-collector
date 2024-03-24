plugins {
    `data-collector-common`
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
}

dependencies {
    implementation(platform(project(":build-platform")))
    implementation(project(":persistence"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.jetbrains.teamcity:teamcity-rest-client")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
