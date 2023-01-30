package dev.zieger.utils.coroutines.parameterMix

import dev.zieger.utils.misc.DoubleProgression
import dev.zieger.utils.misc.FloatProgression
import java.util.*
import kotlin.reflect.KProperty

class ParameterBuilder {

    private val parameter = LinkedList<Parameter<*>>()

    fun build(): List<Parameter<*>> = parameter

    infix fun <T> KProperty<T>.with(constant: T) =
        parameter.add(Parameter.Direct(this, listOf(constant)))

    infix fun <T> KProperty<T>.with(constants: List<T>) =
        parameter.add(Parameter.Direct(this, constants))

    infix fun KProperty<Long>.with(longProgression: LongProgression) =
        parameter.add(Parameter.Direct(this, longProgression.toList()))

    infix fun KProperty<Int>.with(intProgression: IntProgression) =
        parameter.add(Parameter.Direct(this, intProgression.toList()))

    infix fun KProperty<Double>.with(doubleProgression: DoubleProgression) =
        parameter.add(Parameter.Direct(this, doubleProgression.toList()))

    infix fun KProperty<Float>.with(floatProgression: FloatProgression) =
        parameter.add(Parameter.Direct(this, floatProgression.toList()))

    infix fun KProperty<*>.with(random: Parameter.Random) =
        parameter.add(Parameter.Direct(this, random.values))
}