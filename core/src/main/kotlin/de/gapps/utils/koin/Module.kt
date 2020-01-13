@file:Suppress("unused")

package de.gapps.utils.koin

import org.koin.core.KoinApplication
import org.koin.core.module.Module


fun KoinApplication.modules(vararg modules: Module) = modules(modules.toList())
