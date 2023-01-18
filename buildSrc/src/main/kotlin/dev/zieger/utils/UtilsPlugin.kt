package dev.zieger.utils

import org.gradle.api.Plugin
import org.gradle.api.Project

open class UtilsPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.run {
        ProjectContext(this, extensions.create(
            "utils", UtilsPluginExtension::class.java
        )).run {
            afterEvaluate {
                configRepositories()
                configPlugins()
//            configJacoco()
                if (isAndroid) configAndroid()
                configTasks()
                configDependencies()
                configPublishing()
            }
        }
    }
}
