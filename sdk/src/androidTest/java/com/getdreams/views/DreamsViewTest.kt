/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import android.app.Activity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.getdreams.Dreams
import com.getdreams.Location
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class DreamsViewTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(Activity::class.java)
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        Dreams.setup("clientId", server.url("/").toString())
    }

    @Test
    fun open() {
        server.enqueue(
            MockResponse()
                .setResponseCode(302)
                .addHeader("Location", server.url("/test").toString())
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
        )
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.open("id token", Location.Dreams, Locale.CANADA_FRENCH)
        }
        val initPost = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/", initPost.path)
        assertEquals("POST", initPost.method)
        assertEquals("application/json; utf-8", initPost.getHeader("Content-Type"))
        assertEquals("application/json", initPost.getHeader("Accept"))
        val expectedBody = """{"clientId":"clientId","idToken":"id token","locale":"fr_CA"}"""
        assertEquals(expectedBody, initPost.body.readUtf8())

        val urlLoad = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/test", urlLoad.path)
        assertEquals("GET", urlLoad.method)
    }

    @Test
    fun updateLocale() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.updateLocale(Locale.ROOT)
        }
    }

    @Test
    fun updateIdToken() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.updateIdToken("new_token")
        }
    }

    @Test
    fun canGoBack() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.canGoBack()
        }
    }

    @Test
    fun goBack() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.goBack()
        }
    }
}
