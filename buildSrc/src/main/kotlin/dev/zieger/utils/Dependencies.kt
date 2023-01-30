package dev.zieger.utils

import dev.zieger.utils.Versions.kotestVersion
import dev.zieger.utils.Versions.kotlinCoroutinesVersion
import dev.zieger.utils.Versions.kotlinSerializationVersion
import dev.zieger.utils.Versions.kotlinVersion
import dev.zieger.utils.Versions.mockkVersion
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.dependencies

fun ProjectContext.configDependencies() = dependencies {

    when (isAndroid) {
        true -> {
            add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
            add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion")
        }
        false -> {
            add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
            add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
        }
    }

    add("implementation", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    add("testImplementation", "io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    add("testImplementation", "io.kotest:kotest-assertions-junit5-jvm:$kotestVersion")
    add("testImplementation", "io.kotest:kotest-property-jvm:$kotestVersion")

    add("testImplementation", "io.mockk:mockk:$mockkVersion")
    add("testImplementation", project(":$name"))
}