package de.gapps.utils.core_testing

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.executeNativeBlocking
import de.gapps.utils.coroutines.runCommand
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

interface IWebMock {

    fun prepare()
    fun tearDown()
}

class SslWebMock(
    private val keyPath: InputStream,
    private val keyPass: String,
    private val mockDispatcher: MockResponse.(RecordedRequest) -> Unit
) : IWebMock {

    private val defScope = DefaultCoroutineScope()

    private lateinit var mockWebServer: MockWebServer

    private fun newSocketFactory(keyInputStream: InputStream, pass: String): SSLSocketFactory {
        val serverKeyStorePassword = pass.toCharArray()
        val serverKeyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        serverKeyStore.load(keyInputStream, serverKeyStorePassword)

        val kmfAlgorithm: String = KeyManagerFactory.getDefaultAlgorithm()
        val kmf: KeyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm)
        kmf.init(serverKeyStore, serverKeyStorePassword)

        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(kmfAlgorithm)
        trustManagerFactory.init(serverKeyStore)

        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(kmf.keyManagers, trustManagerFactory.trustManagers, null)
        return sslContext.socketFactory
    }

    override fun prepare() = defScope.launchEx {
        Log.d("start()")
        "su".runCommand()

        mockWebServer = MockWebServer()
        mockWebServer.useHttps(newSocketFactory(keyPath, keyPass), true)
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Log.v("path=${request.path}")
                return MockResponse().apply { mockDispatcher(request) }
            }
        }
        executeNativeBlocking { mockWebServer.start(443) }

        val url = mockWebServer.url("/")
        Log.v("url=$url")
        System.setProperty("https.proxyHost", url.host)
        System.setProperty("https.proxyPort", "${url.port}")
    }.asUnit()

    override fun tearDown() = defScope.launchEx {
        Log.d("")
        System.clearProperty("https.proxyHost")
        System.clearProperty("https.proxyPort")
        executeNativeBlocking { mockWebServer.shutdown() }
    }.asUnit()
}