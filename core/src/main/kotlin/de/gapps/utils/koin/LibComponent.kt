@file:Suppress("unused")

package de.gapps.utils.koin

import org.koin.core.KoinApplication
import org.koin.core.KoinComponent

interface ILibComponent : KoinComponent {

    val kApp: KoinApplication

    override fun getKoin() = kApp.koin
}

data class LibComponent(override val kApp: KoinApplication) : ILibComponent


