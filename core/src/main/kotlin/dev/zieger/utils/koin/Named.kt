@file:Suppress("unused")

package dev.zieger.utils.koin

import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope


inline fun <reified T> Module.single(
    name: String? = null,
    createdAtStart: Boolean = false,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = single(name?.let(::named), createdAtStart, override, definition)

inline fun <reified T> Module.factory(
    name: String? = null,
    override: Boolean = false,
    noinline definition: Definition<T>
): BeanDefinition<T> = factory(name?.let(::named), override, definition)

inline fun <reified T> Scope.get(
    name: String,
    vararg parameters: Any
): T = get(named(name)) { parametersOf(parameters) }
