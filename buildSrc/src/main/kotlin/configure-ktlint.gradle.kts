plugins {
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    val ktlintVersion = "0.49.1"
    kotlin {
        target("src/main/kotlin/**/*.kt")
        ktlint(ktlintVersion)
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.kts", "buildSrc/src/*.kts")
        ktlint(ktlintVersion)
    }
}
