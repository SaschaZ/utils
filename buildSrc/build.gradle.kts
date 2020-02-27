plugins {
    `kotlin-dsl`
    id("org.jetbrains.dokka") version "0.9.17"
}

repositories {
    mavenCentral()
    google()
    jcenter()
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:3.4.2")
}