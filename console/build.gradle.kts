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
    koin

    koTestRunner
    koTestAssertions
    koTestProperty
}