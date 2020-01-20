@file:Suppress("unused")

package de.gapps.utils.testing

import androidx.annotation.StringRes
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.io.InputStream
import java.nio.charset.Charset

interface IResourceProvider {

    fun getString(id: Int): String
    fun readAsset(fileName: String): InputStream
}

interface IWebMockScope {
    val resourceProvider: IResourceProvider
    val request: RecordedRequest
    val response: MockResponse
}

open class WebMockScope(
    override val resourceProvider: IResourceProvider,
    override val request: RecordedRequest,
    override val response: MockResponse
) : IWebMockScope {

    @Suppress("LeakingThis")
    val respond = RespondScope(this)
}

class RespondScope(webMockScope: WebMockScope) : IWebMockScope by webMockScope

class RequestScope(
    resourceProvider: IResourceProvider,
    request: RecordedRequest,
    response: MockResponse,
    val checkPath: (String) -> Boolean
) : WebMockScope(resourceProvider, request, response) {

    fun checkPath() = request.path?.let { checkPath(it) } ?: false
}


infix fun RespondScope.onPath(path: String) =
    RequestScope(resourceProvider, request, response) { it == path }

infix fun RespondScope.onPath(@StringRes pathId: Int) =
    onPath(resourceProvider.getString(pathId))

infix fun RespondScope.onPathStart(pathStart: String) =
    RequestScope(resourceProvider, request, response) { it.startsWith(pathStart) }

infix fun RespondScope.onPathEnd(pathEnd: String) =
    RequestScope(resourceProvider, request, response) { it.endsWith(pathEnd) }

infix fun RespondScope.onPathContains(pathContains: String) =
    RequestScope(resourceProvider, request, response) { it.contains(pathContains) }

infix fun RequestScope.withAsset(assetName: String) {
    withString(resourceProvider.readAsset(assetName).readBytes().toString(Charset.defaultCharset()))
}

infix fun RequestScope.withString(string: String) {
    if (checkPath()) response.setBody(string)
}

fun webMock(
    keyName: String,
    keyPass: String,
    resourceProvider: IResourceProvider,
    block: WebMockScope.() -> Unit
) = SslWebMock(resourceProvider.readAsset(keyName), keyPass) {
    WebMockScope(resourceProvider, it, this).apply { block() }
}