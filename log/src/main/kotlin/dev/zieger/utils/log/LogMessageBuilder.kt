@file:Suppress("ClassName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.misc.startsWithAny
import dev.zieger.utils.time.TimeFormat
import dev.zieger.utils.time.TimeStamp
import kotlin.math.roundToInt

/**
 * Log-Message-Builder
 */
interface ILogMessageBuilder : IFilter<ILogMessageContext> {

    val build: LogMessageBuilderContext.(withwithOriginClassNameName: Boolean, withOriginMethodName: Boolean) -> String

    var logWithOriginClassNameName: Boolean
    var logWithOriginMethodNameName: Boolean

    fun copyLogMessageBuilder(): ILogMessageBuilder
}

open class LogMessageBuilderContext(
    context: ILogMessageContext
) : ILogMessageContext by context {

    companion object {

    }

    fun ILogMessageContext.callOrigin(
        withFilename: Boolean = true,
        withMethod: Boolean = false,
        ignorePackages: List<String> = listOf(
            "dev.zieger.utils",
            "kotlin", "java", "jdk", "io.kotest"
        ),
        ignoreFiles: List<String> = listOf(
            "Controllable.kt", "Observable.kt",
            "ExecuteExInternal.kt",
            "NativeMethodAccessorImpl.java"
        ),
        ignoreMethods: List<String> = listOf(
            "invoke", "invokeSuspend"
        ),
        methodRegex: Regex = """^[a-zA-Z]\w+\$""".toRegex()
    ): String = if (withFilename || withMethod)
        (callOriginException ?: Exception().also { callOriginException = it })
            .callOrigin(withFilename, withMethod, ignorePackages, ignoreFiles, ignoreMethods, methodRegex)
    else ""

    fun time(): String {
        val format = "HH:mm:ss.SSS"
        val postfix = if (delayed)
            (TimeStamp() - createdAt).seconds.roundToInt().let { "+$it" }
        else ""
        return createdAt.formatTime(TimeFormat.CUSTOM(format)) + postfix
    }
}

class LogMessageBuilder(
    override val build: LogMessageBuilderContext.(Boolean, Boolean) -> String = { f, m -> logMessage(f, m) }
) : ILogMessageBuilder {

    companion object {

        fun LogMessageBuilderContext.logMessage(
            includeFilename: Boolean = false,
            includeMethodName: Boolean = includeFilename
        ): String {
            val logTag = messageTag ?: tag
            val prefix = logTag?.let { "[${level.short}|$it|${time()}]" }
                ?: "[${level.short}|${time()}]"
            return "$prefix " +
                    "${callOrigin(includeFilename, includeMethodName)}: " +
                    "$message" +
                    (throwable?.let { "\n${it.stackTraceToString()}\n" } ?: "")
        }
    }

    override var logWithOriginClassNameName: Boolean = false
    override var logWithOriginMethodNameName: Boolean = logWithOriginClassNameName
        set(value) {
            field = value
            if (value)
                logWithOriginClassNameName = true
        }

    override fun call(context: ILogMessageContext) {
        context.builtMessage = LogMessageBuilderContext(context)
            .build(logWithOriginClassNameName, logWithOriginMethodNameName)
    }

    override fun copyLogMessageBuilder(): ILogMessageBuilder =
        LogMessageBuilder(build)
}

fun Exception.callOrigin(
    withFilename: Boolean = true,
    withMethod: Boolean = false,
    ignorePackages: List<String> = listOf(
        "dev.zieger.utils",
        "kotlin", "java", "jdk", "io.kotest"
    ),
    ignoreFiles: List<String> = listOf(
        "Controllable.kt", "Observable.kt",
        "ExecuteExInternal.kt",
        "NativeMethodAccessorImpl.java"
    ),
    ignoreMethods: List<String> = listOf(
        "invoke", "invokeSuspend"
    ),
    methodRegex: Regex = """^([a-zA-Z][a-zA-Z0-9]+)$""".toRegex()
) = stackTrace.also { println(it.joinToString("\n\t")) }.firstOrNull { trace ->
    !trace.className.startsWithAny(*ignorePackages.toTypedArray())
            && trace.fileName?.let { it !in ignoreFiles } == true
            && trace.lineNumber >= 0
}?.run {
    (if (withFilename) {
        "($fileName:${lineNumber})"
    } else "") +
            (if (withMethod) "#${
                stackTrace.callingMethod(
                    ignorePackages, ignoreFiles, ignoreMethods, methodRegex
                )
            }()" else "")
} ?: ""

private fun Array<StackTraceElement>.callingMethod(
    ignorePackages: List<String>,
    ignoreFiles: List<String>,
    ignoreMethods: List<String>,
    methodRegex: Regex = """^[a-zA-Z]\w+\$""".toRegex()
): String? = firstNotNullOfOrNull {
    it.callingMethod(ignorePackages, ignoreFiles, ignoreMethods, methodRegex)
}

private fun StackTraceElement.callingMethod(
    ignorePackages: List<String>,
    ignoreFiles: List<String>,
    ignoreMethods: List<String>,
    methodNameRegex: Regex = """^[a-zA-Z]\w+\$""".toRegex(),
): String? = "$this"
    .takeIf { str -> ignorePackages.none { it !in str } }
    ?.split("(")?.first()
    ?.split("$")
    ?.map { it.removeSurrounding("$") }
    ?.firstOrNull {
        methodNameRegex.matches(it) &&
                it !in ignoreMethods &&
                it !in ignoreFiles
    }
