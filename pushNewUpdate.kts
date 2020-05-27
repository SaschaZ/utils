#!/usr/bin/env kscript
//DEPS io.ktor:ktor-client-apache:1.3.0,io.ktor:ktor-client-gson:1.3.0
//DEPS org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5,org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.5
//DEPS dev.zieger.utils:core:2.2.4

@file:Suppress("UNREACHABLE_CODE")

import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.misc.asUnit
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess


println("pushNewUpdate.kts started")

class GitHubTags : ArrayList<GitHubTagsItem>() {
    companion object {
        suspend fun get(client: HttpClient): GitHubTags =
                client.get("https://api.github.com/repos/SaschaZ/utils/tags")
    }
}

data class GitHubTagsItem(
        val name: String,
        val zipball_url: String,
        val tarball_url: String,
        val commit: GitHubCommit,
        val node_id: String
)

data class GitHubCommit(
        val sha: String,
        val url: String
)


data class JitPack(
        val version: String? = null,
        val isSnapshot: Boolean = false,
        val status: String? = null,
        val latestOk: String? = null,
        val modules: List<String> = emptyList()
) {
    companion object {
        suspend fun get(client: HttpClient): JitPack =
                client.get("https://jitpack.io/api/builds/com.github.SaschaZ/utils/latest")
    }
}

data class SemanticVersion(var major: Int,
                           var minor: Int,
                           var patch: Int) : Comparable<SemanticVersion>, Comparator<SemanticVersion> {
    constructor(version: String) : this(version.major, version.minor, version.patch)

    override fun compareTo(other: SemanticVersion): Int =
            major.compareTo(other.major) * 10.0.pow(6).toInt() +
                    minor.compareTo(other.minor) * 10.0.pow(3).toInt() +
                    patch.compareTo(other.patch)

    override fun compare(o1: SemanticVersion?, o2: SemanticVersion?): Int = o1!!.compareTo(o2!!)

    override fun toString(): String = "$major.$minor.$patch"

    operator fun inc(): SemanticVersion = apply { patch++ }

    operator fun plus(inc: Int): SemanticVersion = apply { patch += inc }

    companion object {

        val String.major get() = split(".")[0].toInt()
        val String.minor get() = split(".")[1].toInt()
        val String.patch get() = split(".")[2].toInt()
    }
}

val String?.semanticVersion get() = this?.let { SemanticVersion(it) }

println("requesting latest version from github")

suspend fun latestTag(): SemanticVersion {
    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }

    val jitPack = JitPack.get(client)
    val gitHub = GitHubTags.get(client).first()
    println("jp: $jitPack; gh: $gitHub")

    val jpSv = jitPack.version.semanticVersion
    val ghSv = gitHub.name.semanticVersion!!
    println("jpSv: $jpSv; ghSv: $ghSv")

    return jpSv?.let { if (it > ghSv) it else ghSv } ?: ghSv
}

fun File.replaceFirst(regex: Regex, replacement: String = "") =
        apply { writeText(readText().replaceFirst(regex, replacement)) }

fun updateProjectGlobals(versionName: SemanticVersion) {
    File("buildSrc/src/main/kotlin/dev/zieger/utils/Globals.kt")
            .replaceFirst("const val version = \".+\"".toRegex(), "const val version = \"$versionName\"")
            .apply {
                val versionNumber = readText().split("\n")
                        .first { it.trim().startsWith("const val versionNumber = ") }
                        .split(" = ")[1].toInt() + 1
                replaceFirst("const val versionNumber = \\d+".toRegex(), "const val versionNumber = $versionNumber")
            }
}

runBlocking {
    val tag = latestTag() + 1
    println("tag: $tag")
    updateProjectGlobals(tag)

    println("git pull".runCommand())
    println("git add buildSrc/src/main/kotlin/dev/zieger/utils/Globals.kt".runCommand())
    println("git commit -m \"$tag\"".runCommand())
    println("git tag -s $tag".runCommand())
    println("git push".runCommand())

    exitProcess(0).asUnit()
}