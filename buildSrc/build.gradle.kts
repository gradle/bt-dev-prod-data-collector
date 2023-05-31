plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.8.21")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.1.0")
    implementation("nu.studer:gradle-jooq-plugin:8.2.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.19.0")
    implementation("gradle.plugin.org.flywaydb:gradle-plugin-publishing:9.19.1")
}
