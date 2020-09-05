//package dev.zieger.utils.coroutines
//
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.mapNotNull
//import kotlinx.coroutines.flow.onEach
//
//
///**
// * Only works if items in flow are already sorted.
// *
// * For example when groupBy with `it / 2`:
// *   wrong input: '130475289'
// *   correct input: '0123456789' -> ['0-[0,1]', '1-[2,3]', '2-[4,5]', '3-[6,7]', '4-[8,9]']
// */
//fun <T, K> Flow<T>.groupByList(key: (T) -> K): Flow<Pair<K, List<T>>> = flow {
//    var currentKey: K? = null
//    val currentValues = ArrayList<T>()
//
//    onEach {
//        val newKey = key(it)
//        val curKey = currentKey
//        when {
//            newKey == curKey || curKey == null -> {
//                currentKey = newKey
//                currentValues += it
//            }
//            else -> {
//                emit(curKey to currentValues)
//                currentValues.clear()
//                currentValues += it
//                currentKey = newKey
//            }
//        }
//    }
//    currentKey?.also { if (currentValues.isNotEmpty()) emit(it to currentValues) }
//}
//
///**
// * Only works if items in flow are already sorted.
// *
// * For example when groupBy with `it / 2`:
// *   wrong input: '130475289'
// *   correct input: '0123456789' -> ['0-[0,1]', '1-[2,3]', '2-[4,5]', '3-[6,7]', '4-[8,9]']
// */
//fun <T, K> Flow<T>.groupByFlow(key: (T) -> K): Flow<Pair<K, Flow<T>>> {
//    var currentKey: K? = null
//
//    return flow {
//        onEach { value ->
//            val k = key(value)
//            var flow: (suspend (T) -> Unit)? = null
//            when (currentKey) {
//                null, k -> currentKey = k
//                else -> flow { flow = { emit(it) } }
//            }
//            flow?.invoke(value)
//        }
//    }
//}
//
//fun <T, O : Any> Flow<T>.mapPrev(block: (cur: T, prev: T) -> O): Flow<O> {
//    var prev: T? = null
//    return mapNotNull { c -> prev?.let { p -> block(c, p) }.also { prev = c } }
//}