package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.log.logPreFilter
import dev.zieger.utils.statemachine.IMatchScope

/**
 * External condition.
 * Is checked at runtime. All External's need to match within a condition.
 */
open class External(private val condition: suspend IMatchScope.() -> Boolean) : Definition {

    suspend fun ILogScope.matchExternal(scope: IMatchScope): Boolean = scope.run {
        condition() logV {
            filter = logPreFilter { next -> if (noLogging) cancel() else next() }
            "#EX $it => ${this@External}"
        }
    }

    override val hasExternal = true

    override fun toString(): String = "External"
}