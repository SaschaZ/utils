package dev.zieger.utils.log2

import io.kotlintest.specs.AnnotationSpec

internal class LogTest : AnnotationSpec() {

    @Test
    fun testTags() {
        Log.tag = "moofoo"
        Log += "woomoo"
        Log += "bamdam"
        Log.v("test", "fooboo", "boofoo")
        Log.logLevel = LogLevel.DEBUG
        Log.v("test")
        Log -= "woomoo"
        Log.d("test delay", hook = delayHook { Thread.sleep(1000); it(this) })
    }
}