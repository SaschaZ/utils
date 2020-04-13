@file:Suppress("unused")

package de.gapps.utils.android

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ISharedPrefsHolder {
    val sharedPreferences: SharedPreferences
}

@Suppress("TestFunctionName")
inline fun <reified T : Any> SharedPreferenceDelegate(
    initial: T,
    key: String? = null
) = object : ReadWriteProperty<ISharedPrefsHolder, T> {

    override fun getValue(thisRef: ISharedPrefsHolder, property: KProperty<*>): T =
        thisRef.sharedPreferences.get(key ?: property.name, initial)

    override fun setValue(thisRef: ISharedPrefsHolder, property: KProperty<*>, value: T) =
        thisRef.sharedPreferences.edit { set(key ?: property.name, value) }
}

inline fun <reified T> SharedPreferences.Editor.set(key: String, value: T) {
    @Suppress("UNCHECKED_CAST")
    when (T::class) {
        Boolean::class -> putBoolean(key, value as Boolean)
        Int::class -> putInt(key, value as Int)
        Long::class -> putLong(key, value as Long)
        Float::class -> putFloat(key, value as Float)
        Double::class -> putFloat(key, (value as Double).toFloat())
        String::class -> putString(key, value as String)
        Set::class -> putStringSet(key, value as Set<String>)
        else -> throw IllegalArgumentException("Unknown type ${T::class}.")
    }
}

inline fun <reified T> SharedPreferences.get(key: String, default: T): T {
    @Suppress("UNCHECKED_CAST")
    return when (T::class) {
        Boolean::class -> getBoolean(key, default as Boolean) as T
        Int::class -> getInt(key, default as Int) as T
        Long::class -> getLong(key, default as Long) as T
        Float::class -> getFloat(key, default as Float) as T
        Double::class -> getFloat(key, (default as Double).toFloat()) as T
        String::class -> getString(key, default as String) as T
        Set::class -> getStringSet(key, default as Set<String>) as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}.")
    }
}
