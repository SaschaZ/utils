import dev.zieger.utils.globalsModule

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "misc"
}

dependencies {
    globalsModule()
}