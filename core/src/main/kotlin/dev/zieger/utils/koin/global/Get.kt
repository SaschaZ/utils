package dev.zieger.utils.koin.global

import dev.zieger.utils.koin.global.GKoin.getDi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier


inline fun <reified T> get(
    key: String,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = getDi(key).kApp.koin.get(qualifier, parameters)

