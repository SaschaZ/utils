package dev.zieger.utils

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun ProjectContext.configTasks() {
    operator fun String.unaryPlus() = rootProject.extra[this] as String

    tasks.withType(Test::class.java) {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }
//    tasks.getByName("dokka") {
//        outputFormat = "html"
//        outputDirectory = "$buildDir/javadoc"
//    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = +"jvmTargetVersion"
        targetCompatibility = +"jvmTargetVersion"
    }
    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xinline-classes")
            jvmTarget = +"jvmTargetVersion"
        }
    }
}