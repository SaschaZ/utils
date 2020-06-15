@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.koin.global

import dev.zieger.utils.koin.global.GKoin.getDi
import dev.zieger.utils.koin.global.GKoin.getKoin
import dev.zieger.utils.koin.global.GKoin.startKoin
import dev.zieger.utils.koin.global.GKoin.stopKoin
import org.koin.core.Koin
import org.koin.dsl.KoinAppDeclaration
import java.util.concurrent.ConcurrentHashMap

/**
 * [GKoin] is a global wrapper for the [startKoin] and [stopKoin] calls to provide global access to them for multiple
 * Koin instances inside the same process. This can be useful when using global Koin calls in a library but still want
 * to make it possible to use an own Koin instance when depending on the library.
 * To differentiate between those Koin instances [GKoin] maps each instance to an unique key [String] value that needs
 * to be provided to all [GKoin] calls ([getDi], [getKoin], [startKoin] and [stopKoin] as well as [get] and [inject]).
 */
object GKoin {

    private val diMap = ConcurrentHashMap<String, GKoinComponent>()

    fun getDi(key: String): GKoinComponent = getDi(key, false)!!

    fun getDi(
        key: String,
        getOnly: Boolean
    ): GKoinComponent? = when {
        !getOnly -> diMap.getOrPut(key) { GKoinComponentImpl() }
        else -> diMap[key]
    }

    fun getKoin(key: String): Koin = getDi(key).getKoin()

    fun getKoin(
        key: String,
        getOnly: Boolean
    ): Koin? = getDi(key, getOnly)?.getKoin()

    fun startKoin(
        key: String,
        appDeclaration: KoinAppDeclaration
    ) = getDi(key).startKoin(appDeclaration)

    fun stopKoin(key: String) = getKoin(key, true)?.close()
}