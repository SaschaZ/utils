import dev.zieger.utils.*

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "utils"
}

dependencies {
    coroutinesModule
    miscModule
    logModule
    observablesModule
    stateMachineModule
    timeModule
}