buildscript {
    repositories {
        mavenCentral()
    }
}

allprojects {
    apply(plugin = "configure-ktlint")
}
