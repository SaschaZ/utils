import dev.zieger.utils.koin

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "koin"
}

dependencies {
    koin
}