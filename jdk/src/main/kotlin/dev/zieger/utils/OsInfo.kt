package dev.zieger.utils

import java.util.*

object OsInfo {

    enum class OsType {
        LINUX,
        MACOS,
        WINDOWS,
        OTHER
    }

    private var cachedType: OsType? = null

    val type: OsType get() = cachedType ?: determineOsType().also { cachedType = it }

    private fun determineOsType(): OsType {
        val name = System.getProperty("os.name").toLowerCase(Locale.getDefault())
        return when {
            name.contains("win") -> OsType.WINDOWS
            name.contains("mac") -> OsType.MACOS
            name.contains("nux")
                    || name.contains("nix")
                    || name.contains("aix") -> OsType.LINUX
            else -> OsType.OTHER
        }
    }
}