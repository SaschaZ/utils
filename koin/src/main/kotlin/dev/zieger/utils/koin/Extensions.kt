package dev.zieger.utils.koin

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass


@JvmOverloads
inline fun <reified T : Any> DI.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getKoin().get(qualifier, parameters)

@JvmOverloads
inline fun <reified T : Any> DI.getOrNull(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T? = getKoin().getOrNull(qualifier, parameters)

fun <T : Any> DI.get(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null
): T = getKoin().get(clazz, qualifier, parameters)

fun <T : Any> DI.getOrNull(
    clazz: KClass<T>,
    qualifier: Qualifier? = null,
    parameters: ParametersDefinition? = null
): T? = getKoin().getOrNull(clazz, qualifier, parameters)

@JvmOverloads
inline fun <reified T : Any> DI.inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null
): Lazy<T> = getKoin().inject(qualifier, mode, parameters)

@JvmOverloads
inline fun <reified T : Any> DI.injectOrNull(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = LazyThreadSafetyMode.SYNCHRONIZED,
    noinline parameters: ParametersDefinition? = null
): Lazy<T?> = getKoin().injectOrNull(qualifier, mode, parameters)

inline fun <reified T : Any> DI.declare(
    instance: T,
    qualifier: Qualifier? = null,
    secondaryTypes: List<KClass<*>> = emptyList(),
    override: Boolean = false
) = getKoin().declare(instance, qualifier, secondaryTypes, override)

inline fun <reified T : Any> DI.getAll(): List<T> = getKoin().getAll()