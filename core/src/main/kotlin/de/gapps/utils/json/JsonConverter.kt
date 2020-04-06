package de.gapps.utils.json

import de.gapps.utils.misc.catch
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json.Default.parse
import kotlinx.serialization.json.Json.Default.stringify
import kotlinx.serialization.serializer

@ImplicitReflectionSerializer
open class JsonConverter {

    inline fun <reified T : Any> T.toJson(): String? =
        catch(null) { stringify(T::class.serializer(), this) }

    inline fun <reified T : Any> List<T>.toJson(): String? =
        catch(null) { stringify(T::class.serializer().list, this) }

    inline fun <reified T : Any> String.fromJson(): T? =
        catch(null) { parse(T::class.serializer(), this) }

    inline fun <reified T : Any> String.fromJsonList(): List<T>? =
        catch(null) { parse(T::class.serializer().list, this) }
}
