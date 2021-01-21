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
import com.getdreams.Credentials
import com.getdreams.Dreams
import com.getdreams.Result
import com.getdreams.Result.Companion.failure
import com.getdreams.Result.Companion.success
import com.getdreams.connections.EventListener
import com.getdreams.connections.webview.LaunchError
import com.getdreams.connections.webview.RequestInterface.OnLaunchCompletion
import com.getdreams.connections.webview.ResponseInterface
import com.getdreams.events.Event
import com.getdreams.posix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_MOVED_PERM
import java.net.HttpURLConnection.HTTP_MOVED_TEMP
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_SEE_OTHER
import java.net.URL
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

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
    private fun verifyTokenRequest(
        uri: Uri,
        jsonBody: JSONObject
    ): Result<InitResponse, LaunchError> {
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
            return failure(LaunchError.UnexpectedError("Unable to open connection", e))
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
                when (connection.responseCode) {
                    HTTP_MOVED_PERM, HTTP_MOVED_TEMP, HTTP_SEE_OTHER, HTTP_NOT_MODIFIED, 307, 308 -> {
                        getHeaderField("Location")?.let {
                            success(InitResponse(it, headerFields["Set-Cookie"]?.filterNotNull()))
                        } ?: failure(
                            LaunchError.UnexpectedError(
                                message = "Location header missing in $responseCode ($responseMessage)",
                                cause = RuntimeException("No location header in redirection response")
                            )
                        )
                    }
                    in HTTP_OK..299 -> {
                        success(InitResponse(getURL().toString(), headerFields["Set-Cookie"]?.filterNotNull()))
                    }
                    422 -> {
                        failure(LaunchError.InvalidCredentials(message = "Invalid token", cause = null))
                    }
                    in HTTP_BAD_REQUEST..499, HTTP_INTERNAL_ERROR -> {
                        failure(
                            LaunchError.HttpError(
                                responseCode,
                                message = "Got message: $responseMessage",
                                cause = null
                            )
                        )
                    }
                    else -> failure(
                        LaunchError.HttpError(
                            responseCode,
                            message = "Got message: $responseMessage",
                            cause = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            failure(LaunchError.UnexpectedError(e.message ?: "Unknown error when trying to verify credentials", e))
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun initializeWebApp(
        clientId: String,
        idToken: String,
        posixLocale: String
    ): Result<String, LaunchError> {
        val jsonBody = JSONObject()
            .put("client_id", clientId)
            .put("token", idToken)
            .put("locale", posixLocale)
        val result = withContext(Dispatchers.IO) {
            verifyTokenRequest(
                Dreams.instance.baseUri,
                jsonBody
            )
        }
        return when (result) {
            is Result.Success -> {
                with(result.value) {
                    // If we got cookies, set them now
                    if (!cookies.isNullOrEmpty()) {
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookies.forEach { cookie ->
                            cookieManager.setCookie(url, cookie)
                        }
                    }
                    return@with success(url)
                }
            }
            is Result.Failure -> {
                failure(result.error)
            }
        }
    }

    override fun launch(credentials: Credentials, locale: Locale?, onCompletion: OnLaunchCompletion) {
        val posixLocale = locale?.posix ?: with(resources.configuration) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locales[0] ?: Locale.ROOT
            } else {
                @Suppress("DEPRECATION")
                this@with.locale ?: Locale.ROOT
            }
        }.posix

        GlobalScope.launch {
            when (val result = initializeWebApp(Dreams.instance.clientId, credentials.idToken, posixLocale)) {
                is Result.Success -> {
                    withContext(Dispatchers.Main) {
                        webView.loadUrl(result.value)
                    }
                    onCompletion.onResult(success(Unit))
                }
                is Result.Failure -> {
                    onCompletion.onResult(failure(result.error))
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
