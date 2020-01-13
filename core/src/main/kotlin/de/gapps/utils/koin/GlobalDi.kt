@file:Suppress("unused")

package de.gapps.utils.koin

import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import java.util.concurrent.ConcurrentHashMap

interface IGlobalDi : ILibComponent {

    fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication
    fun stopKoin()
}

internal class GlobalDi(private var internalKApp: KoinApplication? = null) : IGlobalDi {

    override val kApp: KoinApplication
        get() = internalKApp ?: throw IllegalStateException("Koin was not initialized yet")

    override fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication {
        if (internalKApp != null) throw IllegalStateException("Koin is already initialized")
        return org.koin.core.context.startKoin(appDeclaration).also { internalKApp = it }
    }

    override fun stopKoin() = kApp.koin.close()
}

object GlobalDiHolder : IGlobalDi by GlobalDi(), (String) -> IGlobalDi {

    const val SINGLE_GLOBAL_DI_KEY = "SINGLE_GLOBAL_DI_KEY"
    var defaultGlobalDiKey: String = SINGLE_GLOBAL_DI_KEY

    private val diMap = ConcurrentHashMap<String, IGlobalDi>()

    override fun invoke(key: String) =
        if (key == SINGLE_GLOBAL_DI_KEY) this
        else diMap.getOrPut(key) { GlobalDi() }!!
}

fun startKoin(
    key: String = GlobalDiHolder.defaultGlobalDiKey,
    appDeclaration: KoinAppDeclaration
) = GlobalDiHolder(key).startKoin(appDeclaration)

fun stopKoin(key: String = GlobalDiHolder.defaultGlobalDiKey) = GlobalDiHolder(key).stopKoin()