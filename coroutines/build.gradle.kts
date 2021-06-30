import dev.zieger.utils.globals
import dev.zieger.utils.misc
import dev.zieger.utils.time

plugins {
    id("java")
    id("dev.zieger.utils")
}

utils {
    moduleName = "coroutines"
}

dependencies {
    time
    misc
    globals
}