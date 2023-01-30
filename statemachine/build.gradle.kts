plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "statemachine"
}

dependencies {
    implementation(project(":log"))
    implementation(project(":misc"))
    implementation(project(":coroutines"))
    implementation(project(":observables"))
    implementation(project(":time"))
}