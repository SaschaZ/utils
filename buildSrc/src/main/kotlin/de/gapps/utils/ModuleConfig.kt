package de.gapps.utils

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType

class ModuleConfig(project: Project) : Project by project {

    fun config(type: LibraryType, name: String) {
        dependencies {
            with(Libs) {
                add("implementation", kotlin)
                add("implementation", coroutinesJdk)

                add("implementation", koin)
                add("implementation", jackson)
                add("implementation", slf4jSimple)
            }

            with(Dependencies) { kotlinJunit5() }
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }

        tasks {
            //            dokka {
//                outputFormat = "html"
//                outputDirectory = "$buildDir/javadoc"
//            }
        }

        configurePublishing(type, name)
    }
}