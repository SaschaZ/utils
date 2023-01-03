buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion: String by project
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
    childProjects.forEach { delete(it.value.buildDir) }
}