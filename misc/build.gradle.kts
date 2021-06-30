import dev.zieger.utils.globals

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "misc"
}

dependencies {
    globals
}