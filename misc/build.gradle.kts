
plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "misc"
}

dependencies {
    implementation(project(":globals"))
    implementation(project(":globals"))
}