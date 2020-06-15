@file:Suppress("unused")

package dev.zieger.utils.koin.global

import dev.zieger.utils.koin.global.GKoin.getDi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T> inject(
    key: String,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
) = object : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        getDi(key).getKoin().get(qualifier, parameters)
}