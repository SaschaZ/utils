import dev.zieger.utils.*

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "coroutines"
}

dependencies {
    timeModule
    miscModule
    globalsModule
}