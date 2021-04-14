@file:Suppress("unused")

package dev.zieger.utils.koin

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import kotlin.reflect.KClass

interface DI {

    fun getKoin(): Koin
}

class DiImpl(vararg modules: Module) : DI {

    private val kApp = startKoin {
        module { single<DI> { this@DiImpl } }
        modules(*modules)
    }

    override fun getKoin(): Koin = kApp.koin
}

@JvmOverloads
inline fun <reified T: Any> DI.inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = getKoin().inject(qualifier, mode, parameters)

@JvmOverloads
inline fun <reified T: Any> DI.injectOrNull(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null
): Lazy<T?> = getKoin().injectOrNull(qualifier, mode, parameters)

@JvmOverloads
inline fun <reified T: Any> DI.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)

@JvmOverloads
inline fun <reified T: Any> DI.getOrNull(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T? = getKoin().getOrNull(qualifier, parameters)

fun <T: Any> DI.get(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null
): T = getKoin().get(clazz, qualifier, parameters)

fun <T: Any> DI.getOrNull(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null
): T? = getKoin().getOrNull(clazz, qualifier, parameters)

inline fun <reified T : Any> DI.declare(
    instance: T,
    qualifier: Qualifier? = null,
    secondaryTypes: List<KClass<*>> = emptyList(),
    override: Boolean = false
) = getKoin().declare(instance, qualifier, listOf(T::class) + secondaryTypes, override)