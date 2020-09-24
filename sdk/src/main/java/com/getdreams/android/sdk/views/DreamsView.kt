/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.android.sdk.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.getdreams.android.sdk.Dreams
import com.getdreams.android.sdk.Location
import com.getdreams.android.sdk.connections.webview.ResponseInterface
import com.getdreams.android.sdk.events.Event
import com.getdreams.android.sdk.posix
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Interface for [DreamsView].
 */
interface DreamsViewI {
    /**
     * Open Dreams at [location].
     *
     * @param accessToken The token used to authenticate.
     * @param location What screen to open, by default [Location.Home].
     */
    fun open(accessToken: String, location: Location = Location.Home, locale: Locale? = null)

    /**
     * Set the locale used in Dreams.
     */
    fun updateLocale(locale: Locale)

    /**
     * Update the access token.
     */
    fun updateAccessToken(accessToken: String)

    /**
     * Returns true if the dreams view can navigate back.
     */
    fun canGoBack(): Boolean

    /**
     * Navigates back in the dreams view.
     */
    fun goBack()
}

/**
 * The main view to present Dreams to users.
 */
class DreamsView : FrameLayout, DreamsViewI {
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        webView = WebView(context, attrs, defStyleAttr)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        webView = WebView(context, attrs, defStyleAttr, defStyleRes)
        init()
    }

    private val webView: WebView
    private var sessionId: String = ""

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
    }

    override fun open(accessToken: String, location: Location, locale: Locale?) {
        val posixLocale = locale?.posix ?: with(resources.configuration) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locales[0] ?: Locale.ROOT
            } else {
                @Suppress("DEPRECATION")
                this@with.locale ?: Locale.ROOT
            }
        }.posix

        val uriBuilder = Dreams.instance.baseUri.buildUpon()
            .appendQueryParameter("locale", posixLocale)

        webView.addJavascriptInterface(object : ResponseInterface {
            @JavascriptInterface
            override fun initialized(sessionId: String?) {
                if (sessionId.isNullOrEmpty()) {
                    Log.w("Dreams", "Initialized without sessionId")
                    return
                }
                this@DreamsView.sessionId = sessionId
                this@DreamsView.initialize(Dreams.instance.clientId, accessToken, posixLocale)
                Dreams.instance.onResponse(Event.Response.Initialized, null)
            }

            @JavascriptInterface
            override fun onAccessTokenDidExpire() {
                Dreams.instance.onResponse(Event.Response.AccessTokenExpired, null)
            }

            @JavascriptInterface
            override fun onOffboardingDidComplete() {
                Dreams.instance.onResponse(Event.Response.OffboardingCompleted, null)
            }
        }, "Native")

        webView.loadUrl(uriBuilder.build().toString())
    }

    /**
     * Initialize the PWA with needed data.
     *
     * @param clientId The id of the client.
     * @param accessToken The user token.
     * @param locale Posix formatted locale to use.
     */
    private fun initialize(clientId: String, accessToken: String, locale: String) {
        if (sessionId.isEmpty()) {
            Log.v("Dreams", "No valid session id")
            return
        }
        val jsonData: JSONObject = JSONObject()
            .put("sessionId", sessionId)
            .put("clientId", clientId)
            .put("accessToken", accessToken)
            .put("locale", locale)
        webView.evaluateJavascript("initialize(${jsonData})") {
            Log.v("Dreams", "initialize returned $it")
        }
    }

    override fun updateLocale(locale: Locale) {
        val jsonData: JSONObject = JSONObject()
            .put("sessionId", sessionId)
            .put("locale", locale.posix)
        webView.evaluateJavascript("updateLocale(${jsonData})") {
            Log.v("Dreams", "updateLocale returned $it")
        }
    }

    override fun updateAccessToken(accessToken: String) {
        val jsonData: JSONObject = JSONObject()
            .put("sessionId", sessionId)
            .put("accessToken", accessToken)
        webView.evaluateJavascript("updateAccessToken(${jsonData})") {
            Log.v("Dreams", "updateAccessToken returned $it")
        }
    }

    override fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    override fun goBack() {
        webView.goBack()
    }
}
