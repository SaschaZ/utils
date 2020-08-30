package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.nullWhenBlank

internal class UtilsAssertException(
    private val type: AssertType,
    private val extraMessage: String,
    private val actual: Any?,
    private val expected: Any? = null
) : Throwable() {

    private fun buildMessage(): String = when (expected) {
        null -> "${extraMessage.nullWhenBlank()?.let { "Message: $it\n" } ?: ""}}Type: $type\nactual: $actual\n"
        else -> "${
            extraMessage.nullWhenBlank()?.let { "Message: $it\n" } ?: ""
        }Type: $type\nexpected: $expected\nactual: $actual\n"
    }

    override fun toString(): String = buildMessage()
}