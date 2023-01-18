import dev.zieger.utils.globalsModule

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "misc"
}

dependencies {
    globalsModule
}