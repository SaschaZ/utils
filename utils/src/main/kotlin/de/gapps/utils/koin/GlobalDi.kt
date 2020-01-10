@file:Suppress("unused")

package de.gapps.utils.koin

import de.gapps.utils.koin.GlobalDiHolder.SINGLE_GLOBAL_DI
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

    const val SINGLE_GLOBAL_DI = "SINGLE_GLOBAL_DI"

    private val diMap = ConcurrentHashMap<String, IGlobalDi>()

    override fun invoke(key: String) =
        if (key == SINGLE_GLOBAL_DI) this
        else diMap.getOrPut(key) { GlobalDi() }!!
}

fun startKoin(
    key: String = SINGLE_GLOBAL_DI,
    appDeclaration: KoinAppDeclaration
) = GlobalDiHolder(key).startKoin(appDeclaration)

fun stopKoin(key: String = SINGLE_GLOBAL_DI) = GlobalDiHolder(key).stopKoin()