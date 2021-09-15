import dev.zieger.utils.*

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "log"
}

dependencies {
    coroutinesModule
    timeModule
    miscModule

    coroutinesCore
}