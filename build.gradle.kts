buildscript {
    apply(from = "dependencies.gradle")
    operator fun String.unaryPlus() = rootProject.extra[this] as String

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = +"kotlinVersion"))
        classpath(kotlin("serialization", version = +"kotlinVersion"))
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
    childProjects.forEach { delete(it.value.buildDir) }
}