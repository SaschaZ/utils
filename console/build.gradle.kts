import dev.zieger.utils.*

plugins {
    id("dev.zieger.utils")
    id("java")
}

utils {
    moduleName = "console"
}

dependencies {
    misc

    lanterna

    koTestRunner
    koTestAssertions
    koTestProperty
}