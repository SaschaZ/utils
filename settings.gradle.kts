pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "utils"

include(
    "android",
    "android-testing",
    "core",
    "core-testing",
    "jdk",
    "jdk-testing"
)
