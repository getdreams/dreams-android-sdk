/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import com.getdreams.Dreams
import com.getdreams.connections.EventListener
import com.getdreams.connections.webview.ResponseInterface
import com.getdreams.posix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import com.getdreams.Result
import com.getdreams.Credentials
import com.getdreams.events.Event
import org.json.JSONException
import org.json.JSONTokener
import java.net.HttpURLConnection.HTTP_MOVED_PERM
import java.net.HttpURLConnection.HTTP_MOVED_TEMP
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_SEE_OTHER

/**
 * The main view to present Dreams to users.
 */
class DreamsView : FrameLayout, DreamsViewInterface {
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        webView = WebView(context, attrs, defStyleAttr)
        init()
    }

    @Suppress("unused")
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        webView = WebView(context, attrs, defStyleAttr, defStyleRes)
        init()
    }

    private val webView: WebView
    private val responseListeners = CopyOnWriteArrayList<EventListener>()

    /**
     * Add and setup the web view.
     */
    private fun init() {
        addView(webView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        webView.apply {
            settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
                domStorageEnabled = true
                // Fix for CVE-2020-16873
                setSupportMultipleWindows(true)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    val cache = File(context.cacheDir, "dreams")
                    try {
                        cache.mkdirs()
                    } catch (e: SecurityException) {
                        Log.e("Dreams", "Failed to create directories in cache", e)
                    }

                    @Suppress("DEPRECATION")
                    setAppCachePath(cache.absolutePath)
                    @Suppress("DEPRECATION")
                    setAppCacheEnabled(true)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    offscreenPreRaster = true
                }
            }
        }

        webView.addJavascriptInterface(object : ResponseInterface {
            @JavascriptInterface
            override fun onIdTokenDidExpire(requestData: String) {
                try {
                    val json = JSONTokener(requestData).nextValue() as? JSONObject?
                    val requestId = json?.getString("requestId")
                    if (requestId != null) {
                        this@DreamsView.onResponse(Event.CredentialsExpired(requestId))
                    }
                } catch (e: JSONException) {
                    Log.w("Dreams", "Unable to parse request data", e)
                }
            }

            @JavascriptInterface
            override fun onTelemetryEvent(data: String) {
                try {
                    val json = JSONTokener(data).nextValue() as? JSONObject?
                    val name = json?.getString("name")
                    if (name != null) {
                        val event = Event.Telemetry(name = name, payload = json.optJSONObject("payload"))
                        Log.v("Dreams", "Got telemetry event: $event")
                        this@DreamsView.onResponse(event)
                    }
                } catch (e: JSONException) {
                    Log.w("Dreams", "Unable to parse telemetry", e)
                }
            }

            @JavascriptInterface
            override fun onAccountProvisionRequested(requestData: String) {
                try {
                    val json = JSONTokener(requestData).nextValue() as? JSONObject?
                    val requestId = json?.getString("requestId")
                    if (requestId != null) {
                        this@DreamsView.onResponse(Event.AccountProvisionRequested(requestId))
                    }
                } catch (e: JSONException) {
                    Log.w("Dreams", "Unable to parse request data", e)
                }
            }

            @JavascriptInterface
            override fun onExitRequested() {
                this@DreamsView.onResponse(Event.ExitRequested)
            }
        }, "JSBridge")
    }

    /**
     * The response from the init call.
     */
    internal data class InitResponse(val url: String, val cookies: List<String>?)

    /**
     * Make the initial request to the web app.
     */
    private fun makeInitRequest(
        uri: Uri,
        jsonBody: JSONObject
    ): Result<InitResponse> {
        val url = URL(
            uri.buildUpon()
                .appendPath("users")
                .appendPath("verify_token")
                .build()
                .toString()
        )
        val connection = try {
            url.openConnection() as? HttpURLConnection? ?: throw NullPointerException("Connection was null")
        } catch (e: Exception) {
            return Result.Error(e)
        }

        connection.apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; utf-8")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            doInput = false
            instanceFollowRedirects = false
        }

        return try {
            with(connection) {
                outputStream.write(jsonBody.toString().toByteArray())
                Log.v("Dreams", "Init returned $responseCode ($responseMessage)")
                when (connection.responseCode) {
                    HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER -> {
                        getHeaderField("Location")?.let {
                            Result.Success(InitResponse(it, headerFields["Set-Cookie"]?.filterNotNull()))
                        } ?: Result.Error(RuntimeException("No location header from init"))
                    }
                    HTTP_OK -> {
                        Result.Success(InitResponse(getURL().toString(), headerFields["Set-Cookie"]?.filterNotNull()))
                    }
                    else -> Result.Error(Exception("Unexpected response code: $responseCode ($responseMessage)"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun getUrl(clientId: String, idToken: String, posixLocale: String): String? {
        val jsonBody = JSONObject()
            .put("client_id", clientId)
            .put("token", idToken)
            .put("locale", posixLocale)
        val result = withContext(Dispatchers.IO) {
            makeInitRequest(
                Dreams.instance.baseUri,
                jsonBody
            )
        }
        return when (result) {
            is Result.Success<InitResponse> -> {
                with(result.data) {
                    // If we got a cookie set it now
                    if (!cookies.isNullOrEmpty()) {
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookies.forEach { cookie ->
                            cookieManager.setCookie(url, cookie)
                        }
                    }
                    return@with url
                }
            }
            is Result.Error -> {
                Log.e("Dreams", "Unable to initialize web app", result.exception)
                null
            }
        }
    }

    override fun open(credentials: Credentials, location: String, locale: Locale?) {
        val posixLocale = locale?.posix ?: with(resources.configuration) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locales[0] ?: Locale.ROOT
            } else {
                @Suppress("DEPRECATION")
                this@with.locale ?: Locale.ROOT
            }
        }.posix

        GlobalScope.launch {
            val url = getUrl(Dreams.instance.clientId, credentials.idToken, posixLocale)
            withContext(Dispatchers.Main) {
                if (url != null) {
                    webView.loadUrl(url)
                }
            }
        }
    }

    override fun updateLocale(locale: Locale) {
        val jsonData: JSONObject = JSONObject()
            .put("locale", locale.posix)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            webView.evaluateJavascript("updateLocale('${jsonData}')") {
                Log.v("Dreams", "updateLocale returned $it")
            }
        }
    }

    override fun updateCredentials(requestId: String, credentials: Credentials) {
        val jsonData: JSONObject = JSONObject()
            .put("requestId", requestId)
            .put("idToken", credentials.idToken)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            webView.evaluateJavascript("updateIdToken('${jsonData}')") {
                Log.v("Dreams", "updateIdToken returned $it")
            }
        }
    }

    override fun accountProvisionInitiated(requestId: String) {
        val jsonData: JSONObject = JSONObject()
            .put("requestId", requestId)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            webView.evaluateJavascript("accountProvisionInitiated('${jsonData}')") {
                Log.v("Dreams", "accountProvisioned returned $it")
            }
        }
    }

    override fun registerEventListener(listener: EventListener): Boolean {
        return responseListeners.add(listener)
    }

    override fun removeEventListener(listener: EventListener): Boolean {
        return responseListeners.remove(listener)
    }

    override fun clearEventListeners() {
        responseListeners.clear()
    }

    internal fun onResponse(type: Event) {
        responseListeners.forEach { it.onEvent(type) }
    }

    override fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    override fun goBack() {
        webView.goBack()
    }
}
