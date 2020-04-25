package dev.zieger.utils.json

import dev.zieger.utils.misc.catch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

open class JsonConverter {

    private val json = Json(JsonConfiguration.Stable)

    fun <T : Any> @UnsafeVariance T.toJson(serializer: KSerializer<T>): String? =
        catch(null, onCatch = { print(it) }) { json.stringify(serializer, this) }

    fun <T : Any> List<@UnsafeVariance T>.toJson(serializer: KSerializer<T>): String? =
        catch(null, onCatch = { print(it) }) { json.stringify(serializer.list, this) }

    fun <T : Any> String.fromJson(serializer: KSerializer<T>): T? =
        catch(null, onCatch = { print(it) }) { json.parse(serializer, this) }

    fun <T : Any> String.fromJsonList(serializer: KSerializer<T>): List<T>? =
        catch(null, onCatch = { print(it) }) { json.parse(serializer.list, this) }
}
