import dev.zieger.utils.coroutines
import dev.zieger.utils.misc
import dev.zieger.utils.observables
import dev.zieger.utils.time

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "log"
}

dependencies {
    coroutines
    time
    misc
    observables
}