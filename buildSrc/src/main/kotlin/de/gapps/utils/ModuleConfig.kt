package de.gapps.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

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

        configurePublishing(type, name)
    }
}