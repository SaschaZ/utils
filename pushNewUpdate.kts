#!/usr/bin/env kscript
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOnMaven("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
@file:DependsOnMaven("io.ktor:ktor-client-apache:1.3.2")
@file:DependsOnMaven("io.ktor:ktor-client-gson:1.3.2")
@file:DependsOnMaven("dev.zieger.utils:core:2.2.48")
@file:MavenRepository("jitpack", "https://jitpack.io")

@file:Suppress("UNREACHABLE_CODE", "PropertyName", "MemberVisibilityCanBePrivate")

import dev.zieger.utils.coroutines.CommandOutput
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhenBlank
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.pow
import kotlin.system.exitProcess

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
            client.get("https://jitpack.io/api/builds/dev.zieger/utils/latest")
    }
}

class SemanticVersion(
    major: Int,
    minor: Int,
    var patch: Int,
    var label: String? = null
) : Comparable<SemanticVersion>, Comparator<SemanticVersion> {
    constructor(version: String) : this(version.major, version.minor, version.patch, version.label)

    var major: Int by OnChanged(major) {
        this@SemanticVersion.minor = 0
    }
    var minor: Int by OnChanged(minor) {
        patch = 0
    }

    fun update(version: SemanticVersion) {
        major = version.major
        minor = version.minor
        patch = version.patch
        label = version.label
    }

    override fun compareTo(other: SemanticVersion): Int =
        major.compareTo(other.major) * 10.0.pow(6).toInt() +
                minor.compareTo(other.minor) * 10.0.pow(3).toInt() +
                patch.compareTo(other.patch)

    override fun compare(o1: SemanticVersion, o2: SemanticVersion): Int = o1.compareTo(o2)

    override fun toString(): String = "$major.$minor.$patch${label?.nullWhenBlank()?.let { "-$it" } ?: ""}"

    companion object {

        private val regex = """([0-9]+)\.([0-9]+)\.([0-9]+-?([a-zA-Z0-9.\-_]?))""".toRegex()

        val String.version
            get() = regex.find(this)?.groupValues
                ?.run { SemanticVersion(get(1).toInt(), get(2).toInt(), get(3).toInt(), getOrNull(4)) }
        val String.major get() = version?.major ?: 0
        val String.minor get() = version?.minor ?: 0
        val String.patch get() = version?.patch ?: 0
        val String.label get() = version?.label
    }
}

fun <T : Comparable<T>> max(vararg values: T) = values.toList().max()!!

val String?.semanticVersion get() = this?.let { SemanticVersion(it) }

suspend fun latestTag(): SemanticVersion {
    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
    }

    val jitPack = JitPack.get(client).version.semanticVersion!!
    val gitHub = GitHubTags.get(client).first().name.semanticVersion!!
    val git = "git describe --tag --abbrev=0".runCommand()?.stdOutput.semanticVersion!!

    return max(jitPack, gitHub, git)
}

fun File.replaceFirst(regex: Regex, replacement: String = "") =
    apply { writeText(readText().replaceFirst(regex, replacement)) }

val GLOBAL_FILE = "buildSrc/src/main/kotlin/dev/zieger/utils/Globals.kt"

fun updateProjectGlobals(versionName: SemanticVersion) {
    print("update project globals for $versionName … ")
    File(GLOBAL_FILE)
        .replaceFirst("const val version = \".+\"".toRegex(), "const val version = \"$versionName\"")
        .apply {
            val versionNumber = readText().filterNot { it.anyOf("'", "\"") }.split("\n")
                .first { it.trim().startsWith("const val versionNumber = ") }
                .split(" = ")[1].toInt() + 1
            replaceFirst("const val versionNumber = \\d+".toRegex(), "const val versionNumber = $versionNumber")
        }
    println("done")
}

fun updateReadme(tag: SemanticVersion? = null) {
    print("copy changelog into readme … ")
    File("README.md").apply {
        writeText(readText().updateChangeLog().run { tag?.let { t -> updateVersions(t) } ?: this })
    }
    println("done")
}

fun String.updateVersions(tag: SemanticVersion): String = replace(""""dev\.zieger\.utils:(.+):.+"""".toRegex()) {
    "\"dev.zieger.utils:${it.groupValues[1]}:$tag\""
}

fun String.updateChangeLog(): String = replaceFirst(
    """(# Changelog[\w\W]+$)""".toRegex(),
    File("CHANGELOG.md").readText()
)

private val CommandOutput?.ok
    get() = if (this?.code == 0) "ok" else
        throw IllegalStateException(
            "code: ${this?.code}\n" +
                    "stdOut: ${this?.stdOutput?.reader()?.readText()}\n" +
                    "errOut: ${this?.errOutput?.reader()?.readText()}"
        )

suspend fun updateGit(tag: SemanticVersion) {
    print("git pull … "); println("git pull".runCommand().ok)
    print("git add … "); println("git add $GLOBAL_FILE README.md".runCommand().ok)
    print("git commit … "); println("git commit -m \"$tag\"".runCommand().ok)
    print("git tag … "); println("git tag $tag".runCommand().ok)
    print("git push … "); println("git push".runCommand().ok)
    print("git push tags … "); println("git push --tags".runCommand().ok)
}

fun showHelp() {
    println(
        """
Parameter | Function
----------------------------------------
-r        | Update the README.md
-p        | Update project globals
-g        | Update git
--major   | Increase major version by 1
--minor   | Increase minor version by 1
-f        | Force version

Example      | Description
-----------------------------------------------------------------
-rpg         | Update all with patch version increased by 1
-rpg --major | Update all with major version increased by 1
-r           | Update README.md with patch version increased by 1
-rpgf 2.3.0  | Update all with version set to 2.3.0 
    """
    )
}

suspend fun checkForUncommittedChanges(): Boolean =
    "git status".runCommand()?.stdOutput?.contains("nichts zu committen, Arbeitsverzeichnis unverändert") ?: false

runBlocking {
    if (args.contains("--help")) {
        showHelp()
        exitProcess(0).asUnit()
    }

//    if (!checkForUncommittedChanges()) {
//        System.err.println("There are uncommitted changes.")
//        exitProcess(1)
//    }

    val joinedArgs = args.filter { !it.startsWith("--") }.joinToString { it.removePrefix("-") }
    print("build new tag … ")
    val tag = when {
        joinedArgs.contains("f") -> SemanticVersion(args[args.indexOfLast { it.contains("f") } + 1])
        else -> latestTag().apply {
            when {
                args.contains("--major") -> major++
                args.contains("--minor") -> minor++
                else -> patch++
            }
        }
    }
    println("$tag")

    if (joinedArgs.contains("r")) updateReadme(tag)
    if (joinedArgs.contains("p")) updateProjectGlobals(tag)
    if (joinedArgs.contains("g")) updateGit(tag)

    exitProcess(0).asUnit()
}
