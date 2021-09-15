import dev.zieger.utils.*

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "coroutines"
}

dependencies {
    timeModule()
    miscModule()
    globalsModule()
}