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

dependencies {
    implementation("com.android.tools.build:gradle:3.6.3")
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(kotlin("stdlib"))
}