import dev.zieger.utils.coroutinesModule
import dev.zieger.utils.miscModule
import dev.zieger.utils.timeModule

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
}