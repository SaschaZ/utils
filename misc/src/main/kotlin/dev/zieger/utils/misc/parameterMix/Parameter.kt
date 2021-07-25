package dev.zieger.utils.misc.parameterMix

import kotlin.reflect.KProperty

sealed class Parameter {
    abstract val property: KProperty<*>
    abstract val values: List<Number>
    private var nextIdx = 0
    val nextValue: Number
        get() = values[nextIdx++ % values.size]
    val amount: Int
        get() = values.size

    open class Direct(override val property: KProperty<*>, override val values: List<Number>) : Parameter()
    open class Random(property: KProperty<*>, amount: Int, type: TYPE, min: Number, max: Number) : Direct(property,
        (0..amount).map {
            when (type) {
                TYPE.FLOAT -> kotlin.random.Random.nextFloat() * (max.toFloat() - min.toFloat()) + min.toFloat()
                TYPE.DOUBLE -> kotlin.random.Random.nextDouble() * (max.toDouble() - min.toDouble()) + min.toDouble()
                TYPE.INT -> kotlin.random.Random.nextInt() % (max.toInt() - min.toInt()) + min.toInt()
                TYPE.LONG -> kotlin.random.Random.nextLong() % (max.toLong() - min.toLong()) + min.toLong()
            }
        }) {
        companion object {
            enum class TYPE { FLOAT, DOUBLE, INT, LONG }
        }
    }
}