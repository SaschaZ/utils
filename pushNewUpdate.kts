#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-okhttp:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.request
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess


println("pushNewUpdate.kts started")

class Tags : ArrayList<TagsItem>() {
    companion object {
        suspend fun get(client: HttpClient): Tags =
                client.get("https://api.github.com/repos/SaschaZ/utils/tags")
    }
}
data class TagsItem(
        val name: String,
        val zipball_url: String,
        val tarball_url: String,
        val commit: Commit,
        val node_id: String
)
data class Commit(
        val sha: String,
        val url: String
)

println("requesting latest version from github")

suspend fun buildNewTagName(): String? {
    val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }

    return Tags.get(client).let { tags ->
        client.close()
        tags.firstOrNull()?.let { tag ->
            val (major, minor, patch) = tag.name.split(".")
            "$major.$minor.${patch.toInt() + 1}"
        }
    }
}

fun updateProjectGlobals() {

}

fun commit() {

}

fun tag(newTagName: String) {

}

fun push() {

}