plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.4.32")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:2.5.12")
    implementation("nu.studer:gradle-jooq-plugin:5.2.1")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.17.1")
}
