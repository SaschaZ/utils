package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.nullWhenBlank
import junit.framework.AssertionFailedError

class UtilsAssertException(
    val type: AssertType,
    val extraMessage: String,
    val actual: Any?,
    val expected: Any? = null
) : AssertionFailedError() {

    private fun buildMessage(): String =
        (extraMessage.nullWhenBlank()?.let { "message: $it\n" } ?: "") +
                "type: $type\n" + when (expected) {
            null -> "actual: $actual\n"
            else -> "expected:\t$expected\nactual:\t\t$actual\n"
        }

    override fun toString(): String = buildMessage()
}