package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.nullWhenBlank

class UtilsAssertException(
    val type: AssertType,
    val extraMessage: String,
    val actual: Any?,
    val expected: Any? = null
) : Throwable() {

    private fun buildMessage(): String = when (expected) {
        null -> "${extraMessage.nullWhenBlank()?.let { "Message: $it\n" } ?: ""}Type: $type\nactual: $actual\n"
        else -> "${
            extraMessage.nullWhenBlank()?.let { "Message: $it\n" } ?: ""
        }Type: $type\nexpected: $expected\nactual: $actual\n"
    }

    override fun toString(): String = buildMessage()
}