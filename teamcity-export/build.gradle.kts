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
    implementation("org.jetbrains.teamcity:teamcity-rest-client")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}