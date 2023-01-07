package dev.zieger.utils.misc

import io.kotest.core.spec.style.AnnotationSpec
import java.awt.Color

class ColorHexKtTest : AnnotationSpec() {

    @Test
    fun testInvert() {
        val input = Color.GREEN

        println("${input.formatArgb()} -> ${input.inverted.formatArgb()}")
    }
}