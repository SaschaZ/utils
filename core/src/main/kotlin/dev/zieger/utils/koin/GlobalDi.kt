@file:Suppress("unused")

package dev.zieger.utils.koin

import dev.zieger.utils.koin.GlobalDiHolder.SINGLE_GLOBAL_DI_KEY
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import java.util.concurrent.ConcurrentHashMap

interface IGlobalDi : ILibComponent {

    fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication
    fun stopKoin()
}

internal class GlobalDi : IGlobalDi {

    override lateinit var kApp: KoinApplication

    override fun startKoin(appDeclaration: KoinAppDeclaration) =
        org.koin.core.context.startKoin(appDeclaration).also { kApp = it }

    override fun stopKoin() = kApp.koin.close()
}

object GlobalDiHolder : IGlobalDi by GlobalDi(), (String, Boolean) -> IGlobalDi? {

    const val SINGLE_GLOBAL_DI_KEY = "SINGLE_GLOBAL_DI_KEY"

    private val diMap = ConcurrentHashMap<String, IGlobalDi>()

    override fun invoke(key: String, getOnly: Boolean) =
        if (key == SINGLE_GLOBAL_DI_KEY) this
        else if (!getOnly) diMap.getOrPut(key) { GlobalDi() }!!
        else diMap.getOrDefault(key, null)

    fun getDiMapCopy() = HashMap(diMap)
}

fun getKoinComponent(
    key: String = SINGLE_GLOBAL_DI_KEY
) = getKoinComponent(key, false)!!

fun getKoinComponent(
    key: String = SINGLE_GLOBAL_DI_KEY,
    getOnly: Boolean
) = GlobalDiHolder(key, getOnly)

fun startKoin(
    key: String = SINGLE_GLOBAL_DI_KEY,
    appDeclaration: KoinAppDeclaration
) = getKoinComponent(key).startKoin(appDeclaration)

fun stopKoin(key: String = SINGLE_GLOBAL_DI_KEY) =
    getKoinComponent(key, true)?.stopKoin()