import dev.zieger.utils.koTestAssertions
import dev.zieger.utils.koTestProperty
import dev.zieger.utils.koTestRunner
import dev.zieger.utils.lanterna

plugins {
    id("dev.zieger.utils")
    id("java")
}

utils {
    moduleName = "console"
}

dependencies {
    lanterna

    koTestRunner
    koTestAssertions
    koTestProperty
}