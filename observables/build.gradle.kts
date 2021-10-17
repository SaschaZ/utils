import dev.zieger.utils.timeModule

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "observables"
}

dependencies {
    timeModule
}