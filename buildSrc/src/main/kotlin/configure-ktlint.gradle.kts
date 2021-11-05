plugins {
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    val ktlintVersion = "0.43.0"
    kotlin {
        target("src/main/kotlin/**/*.kt")
        ktlint(ktlintVersion)
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.kts", "buildSrc/src/*.kts")
        ktlint(ktlintVersion)
    }
}
