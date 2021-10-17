buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val kotlinVersion: String by project
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))

//        classpath("org.gradle.java-library")
    }
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}