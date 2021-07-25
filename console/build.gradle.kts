import dev.zieger.utils.*

plugins {
    id("dev.zieger.utils")
    id("java")
}

utils {
    moduleName = "console"
}

dependencies {
    misc

    lanterna
    implementation(project(":koin"))
    koin

    koTestRunner
    koTestAssertions
    koTestProperty
}