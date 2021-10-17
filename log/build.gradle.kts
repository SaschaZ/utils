import dev.zieger.utils.*
import dev.zieger.utils.Config.API

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "log"
}

dependencies {
    coroutinesModule
    timeModule
    miscModule

    coroutinesCore(API)
}