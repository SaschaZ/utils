
plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "log"
}

dependencies {
    implementation(project(":time"))
    implementation(project(":misc"))
}