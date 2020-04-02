package dev.zieger.utils.misc

import java.io.File
import java.net.URLDecoder

object JarLocation {

    val directory: String
        get() = JarLocation::class.java.protectionDomain?.codeSource?.location?.path?.let { path ->
            val decoded = URLDecoder.decode(path, "UTF-8")
            var lastIndexOf = decoded.lastIndexOf(File.separator)
            if (lastIndexOf < 0) lastIndexOf = decoded.lastIndexOf("/")
            decoded.substring(0, lastIndexOf)
        } ?: throw IllegalStateException("Could not provide Jar location")

    fun fileInSameDir(filename: String) = File("$directory${File.separator}$filename")
}