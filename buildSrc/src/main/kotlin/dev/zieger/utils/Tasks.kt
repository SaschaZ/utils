package dev.zieger.utils

import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun ProjectContext.configTasks() {
    tasks.withType(Test::class.java) {
        useJUnitPlatform()
        outputs.upToDateWhen { false }
    }
//    tasks.getByName("dokka") {
//        outputFormat = "html"
//        outputDirectory = "$buildDir/javadoc"
//    }

    val jvmTargetVersion by GradleProperty()
    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xinline-classes")
            jvmTarget = jvmTargetVersion
        }
    }
}