package dev.zieger.utils

import java.io.FileInputStream
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class GradleProperty<P> : ReadOnlyProperty<P, String> {
    companion object {
        const val GRADLE_PROPERTIES = "buildSrc/gradle.properties"
    }

    override fun getValue(thisRef: P, property: KProperty<*>): String =
        Properties().apply { load(FileInputStream(GRADLE_PROPERTIES)) }.getProperty(property.name)
}

object Versions {
    val kotlinVersion by GradleProperty()
    val kotlinCoroutinesVersion by GradleProperty()
    val kotlinSerializationVersion by GradleProperty()
    val ktorVersion by GradleProperty()
    val koinVersion by GradleProperty()
    val mockkVersion by GradleProperty()
    val kotestVersion by GradleProperty()
    val composeVersion by GradleProperty()
    val exposedVersion by GradleProperty()
    val dokkaVersion by GradleProperty()
    val jacocoVersion by GradleProperty()
    val oshiVersion by GradleProperty()
}