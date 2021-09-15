buildscript {
    repositories { mavenCentral() }

    dependencies {
        val kotlinVersion: String by project
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
//        classpath(kotlin("serialization", version = kotlinVersion))

//        val androidGradlePluginVersion: String by project
//        classpath("com.android.tools.build:gradle:$androidGradlePluginVersion")
    }
}

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

val androidGradlePluginVersion: String by project
val kotlinVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("com.android.tools.build:gradle:$androidGradlePluginVersion")
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
}