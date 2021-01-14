/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.getdreams.Credentials
import com.getdreams.Dreams
import com.getdreams.R
import com.getdreams.TestActivity
import com.getdreams.events.Event
import com.getdreams.test.utils.getInputStreamFromAssets
import com.getdreams.test.utils.testResponseEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class DreamsViewTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule
    var server = MockWebServer()

    class MockDreamsDispatcher(private val server: MockWebServer) : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                "/users/verify_token" -> MockResponse()
                    .setResponseCode(302)
                    .addHeader("Location", server.url("/index").toString())
                "/index" -> MockResponse()
                    .setResponseCode(200)
                    .setBody(Buffer().readFrom(getInputStreamFromAssets("index.html")))
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    @Before
    fun setup() {
        server.dispatcher = MockDreamsDispatcher(server)
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))
    }

    @After
    fun teardown() {
        Dreams._instance = null
    }

    @Test
    fun open() {
        val latch = CountDownLatch(1)
        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.open(Credentials("id token"), locale = Locale.CANADA_FRENCH)
            dreamsView.registerEventListener { event ->
                when (event) {
                    is Event.Telemetry -> {
                        if ("content_loaded" == event.name) {
                            latch.countDown()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))

        val initPost = server.takeRequest()
        assertEquals("/users/verify_token", initPost.path)
        assertEquals("POST", initPost.method)
        assertEquals("application/json; utf-8", initPost.getHeader("Content-Type"))
        assertEquals("application/json", initPost.getHeader("Accept"))
        val expectedBody = """{"client_id":"clientId","token":"id token","locale":"fr_CA"}"""
        assertEquals(expectedBody, initPost.body.readUtf8())

        val urlLoad = server.takeRequest()
        assertEquals("/index", urlLoad.path)
        assertEquals("GET", urlLoad.method)
    }

    @Test
    fun updateLocale() {
        val latch = CountDownLatch(1)
        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.open(Credentials("token"))
            dreamsView.registerEventListener { event ->
                when (event) {
                    is Event.Telemetry -> {
                        if ("content_loaded" == event.name) {
                            dreamsView.updateLocale(Locale.ROOT)
                            GlobalScope.launch {
                                delay(250)
                                latch.countDown()
                            }
                        }
                    }
                }
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        val open = server.takeRequest()
        assertEquals("/users/verify_token", open.path)
        assertEquals("POST", open.method)
        val urlLoad = server.takeRequest()
        assertEquals("/index", urlLoad.path)
        assertEquals("GET", urlLoad.method)
    }

    @Test
    fun updateIdToken() {
        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("expire_token_button") { event, view ->
            assertEquals(Event.IdTokenExpired("uuid"), event)
            view.updateIdToken((event as Event.IdTokenExpired).requestId, "new token")
            GlobalScope.launch {
                delay(250)
                latch.countDown()
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun accountProvisioned() {
        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("provision_account_button") { event, view ->
            assertEquals(Event.AccountProvisionRequested("uuid"), event)
            view.accountProvisionInitiated((event as Event.AccountProvisionRequested).requestId)
            GlobalScope.launch {
                delay(250)
                latch.countDown()
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun onExitRequested() {
        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("exit_button") { event, _ ->
            assertEquals(Event.ExitRequested, event)
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

    @Test
    fun canGoBack() {
        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.canGoBack()
        }
    }

    @Test
    fun goBack() {
        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.goBack()
        }
    }
}
