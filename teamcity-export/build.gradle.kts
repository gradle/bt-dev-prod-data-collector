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
    implementation("org.jetbrains.teamcity:teamcity-rest-client")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-annotations")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
