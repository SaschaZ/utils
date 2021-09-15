@file:Suppress("SpellCheckingInspection", "unused")

package dev.zieger.utils

import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.project

typealias MD = ModuleDependency
typealias EMD = ExternalModuleDependency
typealias DHS = DependencyHandlerScope

private fun config(forTest: Boolean = false) = if (forTest) "testImplementation" else "implementation"

fun DHS.addModule(name: String, forTest: Boolean = false, action: Action<MD> = Action {}): MD =
    addDependencyTo(this, config(forTest), project(":$name"), action)

fun DHS.add(dependency: String, forTest: Boolean = false, action: Action<EMD> = Action {}): EMD =
    addDependencyTo(this, config(forTest), dependency, action)

val DHS.globalsModule: MD get() = globalsModule()
fun DHS.globalsModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("globals", forTest, action)

val DHS.coroutinesModule: MD get() = coroutines()
fun DHS.coroutines(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("coroutines", forTest, action)

val DHS.koinModule: MD get() = koinModule()
fun DHS.koinModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("koin", forTest, action)

val DHS.logModule: MD get() = logModule()
fun DHS.logModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("log", forTest, action)

val DHS.miscModule: MD get() = miscModule()
fun DHS.miscModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("misc", forTest, action)

val DHS.observablesModule: MD get() = observablesModule()
fun DHS.observablesModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("observables", forTest, action)

val DHS.timeModule: MD get() = timeModule()
fun DHS.timeModule(forTest: Boolean = false, action: Action<MD> = Action { isTransitive = true }): MD =
    addModule("time", forTest, action)

val DHS.kotlin: EMD get() = kotlin()
fun DHS.kotlin(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = false }): EMD =
    add("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}", forTest, action)

val DHS.coroutinesCore: EMD get() = coroutinesCore()
fun DHS.coroutinesCore(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.coroutinesJdk: EMD get() = coroutinesJdk()
fun DHS.coroutinesJdk(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.testCoroutinesCore: EMD get() = testCoroutinesCore()
fun DHS.testCoroutinesCore(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.testCoroutinesJdk: EMD get() = testCoroutinesJdk()
fun DHS.testCoroutinesJdk(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.coroutinesAndroid: EMD get() = coroutinesAndroid()
fun DHS.coroutinesAndroid(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.testCoroutinesAndroid: EMD get() = testCoroutinesAndroid()
fun DHS.testCoroutinesAndroid(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.coroutinesSwing: EMD get() = coroutinesSwing()
fun DHS.coroutinesSwing(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${Versions.kotlinCoroutinesVersion}", forTest, action)

val DHS.kotlinSerialization: EMD get() = kotlinSerialization()
fun DHS.kotlinSerialization(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerializationVersion}", forTest, action)

val DHS.koin: EMD get() = koin()
fun DHS.koin(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("org.koin:koin-core:${Versions.koinVersion}", forTest, action)

val DHS.mockk: EMD get() = mockk()
fun DHS.mockk(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.mockk:mockk:${Versions.mockkVersion}", forTest, action)

val DHS.ktorClientGson: EMD get() = ktorClientGson()
fun DHS.ktorClientGson(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.ktor:ktor-client-gson:${Versions.ktorVersion}", forTest, action)

val DHS.ktorServerGson: EMD get() = ktorServerGson()
fun DHS.ktorServerGson(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.ktor:ktor-jackson:${Versions.ktorVersion}", forTest, action)

val DHS.lanterna: EMD get() = lanterna()
fun DHS.lanterna(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("com.googlecode.lanterna:lanterna:${Versions.lanternaVersion}", forTest, action)

val DHS.koTestRunner: EMD get() = koTestRunner()
fun DHS.koTestRunner(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.kotest:kotest-runner-junit5-jvm:${Versions.kotestVersion}", forTest, action)

val DHS.koTestAssertions: EMD get() = koTestAssertions()
fun DHS.koTestAssertions(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.kotest:kotest-assertions-core-jvm:${Versions.kotestVersion}", forTest, action)

val DHS.koTestProperty: EMD get() = koTestProperty()
fun DHS.koTestProperty(forTest: Boolean = false, action: Action<EMD> = Action { isTransitive = true }): EMD =
    add("io.kotest:kotest-property-jvm:${Versions.kotestVersion}", forTest, action)