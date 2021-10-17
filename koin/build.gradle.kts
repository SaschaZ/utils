import dev.zieger.utils.Config.API
import dev.zieger.utils.koin

plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "koin"
}

dependencies {
    koin(API)
}