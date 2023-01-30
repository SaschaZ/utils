
plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "observables"
}

dependencies {
    implementation(project(":time"))
}