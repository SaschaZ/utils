@file:Suppress("unused")

package dev.zieger.utils.misc

import dev.zieger.utils.coroutines.builder.asyncEx
import dev.zieger.utils.coroutines.executeNativeBlocking
import dev.zieger.utils.log.Log
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.duration.IDurationHolder
import dev.zieger.utils.time.duration.latest
import dev.zieger.utils.time.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

@Deprecated("use other divMod", ReplaceWith("Long.divMod(double): Pair<Double, Double>"), DeprecationLevel.WARNING)
fun Long.divMod(div: Double, modulo: ((Long, Long) -> Unit)? = null) =
    (this / div).also { modulo?.invoke(it.toLong(), (this % div).toLong()) }

@Suppress("UNCHECKED_CAST")
fun <T : Number> T.divMod(div: T): Pair<T, T> = (this / div) as T to (this % div) as T

fun List<Double?>.average(sizeForce: Int? = null, selector: ((Double?) -> Boolean)? = null): Double? {
    val selected = filterNotNull().filter { selector?.invoke(it) ?: true }
    if (selected.isEmpty()) return null
    return selected.sumByDouble { it } / (sizeForce ?: selected.size)
}

infix fun Double?.average(onNull: Double): Double = this?.let { (it + onNull) / 2 } ?: onNull

fun Any?.equalsOne(vararg args: Any) = args.any { args == this }

inline fun <E, K, V> List<E>.merge(key: (E) -> K, combine: (List<E>) -> V?): List<V> {
    val list = ArrayList<V>()
    groupBy { key(it) }.values.forEach { value -> combine(value)?.let { list.add(it) } }
    return list
}

inline infix fun <T : Any> List<T>.take(block: (T) -> Boolean): List<T> =
    mapNotNull { if (block(it)) it else null }

infix fun <K : Any, E : Any> Map<K, List<E>>.removeListValueIf(block: (E) -> Boolean): HashMap<K, List<E>> =
    map { v ->
        val newList = v.value.toMutableList().also { list ->
            list.removeIf { block(it) }
        }
        Pair(v.key, newList)
    }.let { HashMap<K, List<E>>().also { map -> map.putAll(it) } }

infix fun File.writeString(content: String) = printWriter().use { it.print(content) }

fun File.readString() = if (exists()) bufferedReader().readLines().joinToString("\n") else null

fun <T> Iterable<T>.joinToStringIndexed(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: (Int, T) -> CharSequence = { _, value -> value.toString() }
): String {
    var idx = 0
    val transformInternal: ((T) -> CharSequence)? = transform.let { { value -> transform(idx++, value) } }
    return joinToString(separator, prefix, postfix, limit, truncated, transformInternal)
}

inline infix fun <T : Any> Iterable<T>.forEachPrev(block: (cur: T, prev: T) -> Unit) {
    var previous: T? = null
    forEach { value ->
        previous?.let { block(value, it) }
        previous = value
    }
}

inline fun <T : Any, R : Any> Iterable<T?>.mapPrev(
    offset: Int = 1,
    onNull: ((index: Int, cur: T) -> T) = { _, value -> value },
    block: (cur: T, prev: T) -> R?
): List<R?> {
    val list = toList()
    return mapIndexed { index, value ->
        value?.let { block(value, list.getOrNull(index - offset) ?: onNull(index, value)) }
    }
}

inline fun <T : Any, R : Any> Iterable<T?>.mapPrevIndexed(
    offset: Int = 1,
    onNull: ((index: Int, cur: T) -> T) = { _, value -> value },
    block: (index: Int, cur: T, prev: T) -> R?
): List<R?> {
    var index = 0
    return mapPrev(offset, onNull) { cur, prev -> block(index++, cur, prev) }
}

inline fun <T : Any, R : Any> Iterable<T?>.mapPrevNotNull(
    offset: Int = 1,
    onNull: ((index: Int, cur: T) -> T) = { _, value -> value },
    block: (cur: T, prev: T) -> R
): List<R> =
    mapPrev(offset, onNull, block).filterNotNull()

inline fun <T : Any, R : Any> Iterable<T?>.mapPrevIndexedNotNull(
    offset: Int = 1,
    onNull: ((index: Int, cur: T) -> T) = { _, value -> value },
    block: (index: Int, cur: T, prev: T) -> R
): List<R> {
    var index = 0
    return mapPrevNotNull(offset, onNull) { cur, prev -> block(index++, cur, prev) }
}

inline infix fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T> List<T>.lastOrNull(pre: Int = 0) = getOrNull(lastIndex - pre)

infix fun <T> List<T>.last(prevIndex: Int) = get(lastIndex - prevIndex)

infix fun Any?.formatSize(length: Int) = "%${length}s".format(this.toString())

fun Any?.formatQuery() = this?.let { URLEncoder.encode("$it", "UTF-8") } ?: ""

fun Any?.asUnit() = Unit

infix fun Double?.posOr(or: Double) = this?.let { max(or, it) } ?: or

fun <T> List<T>.lastOr(alt: T, prevIndex: Int = 0) = lastOrNull(prevIndex) ?: alt

fun <T> List<T>.getOr(index: Int, alt: T) = getOrNull(index) ?: alt

fun Double.abs() = absoluteValue

inline infix fun <T> List<T>.indexOfLastIndexed(predicate: (T, Int) -> Boolean): Int {
    var index = lastIndex
    return indexOfLast { value ->
        predicate(value, index--)
    }
}

inline infix fun <K, V> ConcurrentHashMap<K, V>.removeInMap(condition: (Map.Entry<K, V>) -> Boolean): ConcurrentHashMap<K, V> {
    forEach { if (condition(it)) remove(it.key) }
    return this
}

fun minInt(vararg values: Number?) = values.minBy { it?.toInt() ?: Int.MAX_VALUE }?.toInt()!!

fun minDouble(vararg values: Number?) = values.minBy { it?.toDouble() ?: Double.MAX_VALUE }?.toDouble()!!

fun maxInt(vararg values: Number?) = values.maxBy { it?.toInt() ?: Int.MIN_VALUE }?.toInt()!!

fun maxDouble(vararg values: Number?) = values.maxBy { it?.toDouble() ?: Double.MIN_VALUE }?.toDouble()!!

infix fun Number.exp10(power: Number) = this * (10 pow power)

suspend fun String?.writeToFile(path: String) = this?.let { content ->
    executeNativeBlocking {
        val file = File(path)
        if (!file.exists()) file.createNewFile()
        file.writeString(content)
        file
    }
}

inline infix fun <K0, V0, K1, V1> ConcurrentHashMap<K0, V0>.mapToMap(
    block: (MutableMap.MutableEntry<K0, V0>) -> Pair<K1, V1>
): ConcurrentHashMap<K1, V1> {
    val newMap = ConcurrentHashMap<K1, V1>()

    entries.forEach { oldMapEntry ->
        block(oldMapEntry).also { toPut ->
            newMap[toPut.first] = toPut.second
        }
    }

    return newMap
}

fun Any?.nullToBlank() = this?.let { it } ?: ""

fun <T : Any?> T?.nullWhenZero() = this?.let { if (it == 0) null else this }

inline infix fun <T : Any?> T?.nullWhen(condition: (T) -> Boolean) = this?.let { if (condition(it)) null else this }

fun <T : Number> T?.negativeToNull() = nullWhen { (it as Number).ex <= 0.ex }

fun List<String?>.toQueryParameter(): String =
    asSequence().filterNotNull().joinToString("&").let { if (it.isNotBlank()) "?$it" else "" }

fun ByteArray.toHex(): String {
    val hexArray = "0123456789abcdef".toCharArray()
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v = get(j).toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

inline fun <reified T : Any> T.toStringGeneric(
    excludeNull: Boolean = true,
    minimumVisibility: KVisibility = KVisibility.PRIVATE
) = "${T::class.java.simpleName}(${T::class.memberProperties
    .filter { kProperty1: KProperty1<T, *>? ->
        kProperty1?.visibility?.ordinal?.let { it <= minimumVisibility.ordinal } == true
                && if (excludeNull) kProperty1.get(this) != null else true
    }.joinToString(", ") { "${it.name}=${it.get(this)}" }})"

infix fun Any.onShutdown(block: suspend () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread(Runnable {
        runBlocking { block() }
    }, "${javaClass.simpleName}-Shutdown-thread"))
}

fun <C : Collection<T>, T> C.nullWhenEmpty(): C? = if (isNotEmpty()) this else null


infix fun <K : Any> ConcurrentHashMap<K, Mutex>.getMutex(key: K): Mutex = getOrPut(key) { Mutex() }

fun <K : Any, V : Any> ConcurrentHashMap<K, ArrayList<V>>.update(key: K, value: V, distinct: Boolean = false): Boolean {
    var isNew = false
    getOrPut(key) {
        isNew = true
        ArrayList()
    }.also { list ->
        if (!distinct || !list.contains(value)) {
            list.add(value)
        }
    }

    return isNew
}

fun <K : Any, V : Any> ConcurrentHashMap<K, ArrayList<V>>.addAll(key: K, value: List<V>) =
    getOrPut(key) { ArrayList() }.addAll(value)

suspend inline fun <reified T : IDurationHolder> processTimeHolderListProducer(
    startTime: TimeEx,
    crossinline request: (endTime: TimeEx?) -> List<T>?
) = CoroutineScope(coroutineContext).asyncEx(null) {
    Log.d("processTimeHolderListProducer() startTime=$startTime")
    val items = ArrayList<T>()
    var startToUse = startTime
    var uniqueItemsReceived = true
    while (uniqueItemsReceived) {
        request(startToUse)?.let { newItems ->
            val uniqueItems = newItems.filter { !items.contains(it) }
            uniqueItemsReceived = uniqueItems.isNotEmpty()
            items.addAll(uniqueItems)
            startToUse = newItems.latest()?.millis?.toTime() ?: startToUse
        } ?: TimeEx(0)
    }
    items
}.await()

fun String.nullWhenBlank() = if (isBlank()) null else this


inline fun <T, R> Collection<T>.runEachIndexed(block: T.(index: Int) -> R): List<R> =
    mapIndexed { idx, value -> value.run { block(idx) } }

inline fun <T, R> Collection<T>.runEach(block: T.() -> R): List<R> = map { it.run(block) }

suspend inline fun <T> ReceiveChannel<T>.runEach(block: T.() -> Unit) {
    for (value in this) value.run { block() }
}

infix fun <T> T.anyOf(values: List<T>) = values.contains(this)
infix fun String.startsWithAny(values: List<String>) = values.any { startsWith(it) }
fun <T> T.anyOf(vararg values: T): Boolean = anyOf(values.toList())