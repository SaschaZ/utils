import dev.zieger.utils.timeModule

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "observables"
}

dependencies {
    timeModule()
}