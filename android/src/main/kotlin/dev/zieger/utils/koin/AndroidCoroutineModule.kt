@file:Suppress("unused")

package dev.zieger.utils.koin

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.coroutines.scope.MainCoroutineScope
import kotlinx.coroutines.cancel
import org.koin.core.KoinApplication
import org.koin.dsl.module

fun androidCoroutineModule() = module {

    single { MainCoroutineScope() }
    single { DefaultCoroutineScope() }
    single { IoCoroutineScope() }
}

fun KoinApplication.cancelAndroidCoroutineScopes() {
    koin.apply {
        get<MainCoroutineScope>().cancel()
        get<DefaultCoroutineScope>().cancel()
        get<IoCoroutineScope>().cancel()
    }
}

fun KoinApplication.resetAndroidCoroutineScopes() {
    koin.apply {
        get<MainCoroutineScope>().reset()
        get<DefaultCoroutineScope>().reset()
        get<IoCoroutineScope>().reset()
    }
}