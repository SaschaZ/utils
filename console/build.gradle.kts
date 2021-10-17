import dev.zieger.utils.*
import dev.zieger.utils.Config.*

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "console"
}

dependencies {
    miscModule
    koinModule

    koin(API)
    lanterna(API)

    koTestRunner
    koTestAssertions
    koTestProperty
}