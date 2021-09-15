import dev.zieger.utils.*

plugins {
    id("dev.zieger.utils")
    id("java")
}

utils {
    moduleName = "console"
}

dependencies {
    miscModule()
    koinModule()

    koin
    lanterna

    koTestRunner
    koTestAssertions
    koTestProperty
}