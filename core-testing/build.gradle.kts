import dev.zieger.utils.ModuleType.JVM_LIB
import dev.zieger.utils.configModule
import dev.zieger.utils.core
import dev.zieger.utils.coroutinesTest
import dev.zieger.utils.mockWebServer

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

configModule("core-testing", JVM_LIB) {
    core
    coroutinesTest
    mockWebServer
}