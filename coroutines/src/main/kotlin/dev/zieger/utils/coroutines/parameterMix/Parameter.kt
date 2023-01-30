package dev.zieger.utils.coroutines.parameterMix

import kotlin.reflect.KProperty

sealed class Parameter<T> {

    abstract val property: KProperty<T>
    abstract val values: List<T>

    private var nextIdx = 0
    val nextValue: T
        get() = values[nextIdx++ % values.size]
    val amount: Int
        get() = values.size

    open class Direct<T>(override val property: KProperty<T>, override val values: List<T>) : Parameter<T>()
    open class Random(property: KProperty<Number>, amount: Int, type: TYPE, min: Number, max: Number) :
        Direct<Number>(property,
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