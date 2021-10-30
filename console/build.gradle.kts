import dev.zieger.utils.*
import dev.zieger.utils.Config.API

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
    coroutinesModule

    koin(API)
    lanterna(API)
    oshi(API)

    koTestRunner
    koTestAssertions
    koTestProperty
}