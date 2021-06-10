
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
