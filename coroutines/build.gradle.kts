plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "coroutines"
}

dependencies {
    implementation(project(":time"))
    implementation(project(":misc"))
    implementation(project(":globals"))
}