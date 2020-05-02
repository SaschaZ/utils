pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

rootProject.name = "utils"

include(
    "android",
    "android-testing",
    "core",
    "core-testing",
    "jdk",
    "jdk-testing",
    "swing"
)

//"channels",
//"console",
