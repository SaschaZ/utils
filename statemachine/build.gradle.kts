import dev.zieger.utils.*

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "statemachine"
}

dependencies {
    log
    misc
    coroutines
    observables
    time
}