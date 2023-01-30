plugins {
    `java-library`
    id("dev.zieger.utils")
}

utils {
    moduleName = "utils"
}

dependencies {
    api(project(":coroutines"))
    api(project(":misc"))
    api(project(":log"))
    api(project(":observables"))
    api(project(":statemachine"))
    api(project(":time"))
}