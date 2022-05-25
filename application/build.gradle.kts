plugins {
    `data-collector-common`
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(platform(project(":build-platform")))
    implementation(project(":ge-export"))
    implementation(project(":teamcity-export"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.slack.api:slack-api-client:1.22.1")
    // TODO: consider -kotlin-extension

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
}

tasks.register("stage") {
    dependsOn("assemble")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<JavaExec>("bootRun") {
    maxHeapSize = "150M"
    jvmArgs("-XX:MaxMetaspaceSize=128M")
}
