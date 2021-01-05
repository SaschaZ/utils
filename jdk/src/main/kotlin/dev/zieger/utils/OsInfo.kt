package dev.zieger.utils

object OsInfo {

    enum class OsType {
        LINUX,
        MACOS,
        WINDOWS,
        OTHER
    }

    private var cachedType: OsType? = null

    val type: OsType get() = cachedType ?: determineOsType().also { cachedType = it }

    private fun determineOsType(): OsType = when (System.getProperty("os.name")) {
        "Windows" -> OsType.WINDOWS
        "Mac OS X" -> OsType.MACOS
        "Linux" -> OsType.LINUX
        else -> OsType.OTHER
    }
}