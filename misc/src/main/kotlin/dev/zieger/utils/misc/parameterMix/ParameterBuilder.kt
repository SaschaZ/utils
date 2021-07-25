package dev.zieger.utils.misc.parameterMix

import dev.zieger.utils.misc.DoubleProgression
import dev.zieger.utils.misc.FloatProgression
import java.util.*
import kotlin.reflect.KProperty

class ParameterBuilder {

    private val parameter = LinkedList<Parameter>()

    fun build(): List<Parameter> = parameter

    infix fun KProperty<*>.with(constant: Number) =
        parameter.add(Parameter.Direct(this, listOf(constant)))

    infix fun KProperty<*>.with(constants: List<Number>) =
        parameter.add(Parameter.Direct(this, constants))

    infix fun KProperty<*>.with(longProgression: LongProgression) =
        parameter.add(Parameter.Direct(this, longProgression.toList()))

    infix fun KProperty<*>.with(intProgression: IntProgression) =
        parameter.add(Parameter.Direct(this, intProgression.toList()))

    infix fun KProperty<*>.with(doubleProgression: DoubleProgression) =
        parameter.add(Parameter.Direct(this, doubleProgression.toList()))

    infix fun KProperty<*>.with(floatProgression: FloatProgression) =
        parameter.add(Parameter.Direct(this, floatProgression.toList()))

    infix fun KProperty<*>.with(random: Parameter.Random) =
        parameter.add(Parameter.Direct(this, random.values))
}