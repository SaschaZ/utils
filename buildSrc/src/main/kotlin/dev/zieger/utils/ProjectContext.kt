package dev.zieger.utils

import org.gradle.api.Project

class ProjectContext(
    project: Project,
    extension: IUtilsPluginExtension
) : Project by project, IUtilsPluginExtension by extension {
    val packageName get() = "dev.zieger.utils.${moduleName.toLowerCase()}"
}

interface IUtilsPluginExtension {
    var isAndroid: Boolean
    var moduleName: String
}

open class UtilsPluginExtension : IUtilsPluginExtension {
    override var isAndroid: Boolean = false
    override var moduleName: String = ""
}