package dev.zieger.utils

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.provideDelegate
import java.io.FileInputStream
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class DepenotProperty<P: Project> : ReadOnlyProperty<P, String> {
    companion object {
        const val DEPENDENCIES_GRADLE = "../dependencies.gradle"
    }

    override fun getValue(thisRef: P, property: KProperty<*>): String =
        thisRef.rootProject.extra[property.name] as String
}

object Versions {

    val Project.kotlinVersion by DepenotProperty()
    val Project.kotlinCoroutinesVersion by DepenotProperty()
    val Project.kotlinSerializationVersion by DepenotProperty()
    val Project.ktorVersion by DepenotProperty()
    val Project.mockkVersion by DepenotProperty()
    val Project.kotestVersion by DepenotProperty()
    val Project.composeVersion by DepenotProperty()
    val Project.exposedVersion by DepenotProperty()
    val Project.dokkaVersion by DepenotProperty()
    val Project.jacocoVersion by DepenotProperty()
    val Project.oshiVersion by DepenotProperty()
}