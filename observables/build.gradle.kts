import dev.zieger.utils.time

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "observables"
}

dependencies {
    time
}