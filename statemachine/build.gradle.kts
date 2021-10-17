import dev.zieger.utils.*

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "statemachine"
}

dependencies {
    logModule
    miscModule
    coroutinesModule
    observablesModule
    timeModule
}