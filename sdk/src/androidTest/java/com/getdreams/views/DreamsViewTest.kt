/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import android.content.Intent
import android.text.Html
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.getdreams.Credentials
import com.getdreams.Dreams
import com.getdreams.R
import com.getdreams.Result
import com.getdreams.TestActivity
import com.getdreams.connections.webview.LaunchError
import com.getdreams.events.Event
import com.getdreams.test.utils.LaunchCompletionWithLatch
import com.getdreams.test.utils.getInputStreamFromAssets
import com.getdreams.test.utils.testResponseEvent
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.anything
import org.hamcrest.Matchers.allOf
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
        Intents.init()
    }

    @After
    fun teardown() {
        Intents.release()
        Dreams._instance = null
    }

    @Test
    fun launch() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)

        val launchCompletion = LaunchCompletionWithLatch()
        val onLaunchCompletion = spyk(launchCompletion)

        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.launch(Credentials("id token"), locale = Locale.CANADA_FRENCH, onLaunchCompletion)
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
        val expectedBody = """{"client_id":"clientId","token":"id token","locale":"fr-CA"}"""
        assertEquals(expectedBody, initPost.body.readUtf8())

        assertTrue(launchCompletion.latch.await(5, TimeUnit.SECONDS))
        verify { onLaunchCompletion.onResult(Result.success(Unit)) }
        confirmVerified(onLaunchCompletion)

        val urlLoad = server.takeRequest()
        assertEquals("/index", urlLoad.path)
        assertEquals("GET", urlLoad.method)
        server.shutdown()
    }

    @Test
    fun launchWithInvalidCredentials() {
        val server = MockWebServer()
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        server.enqueue(MockResponse().setResponseCode(422))

        val launchCompletion = LaunchCompletionWithLatch()
        val onLaunchCompletion = spyk(launchCompletion)

        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.launch(Credentials("fail_auth"), locale = Locale.FRENCH, onLaunchCompletion)
        }

        val initPost = server.takeRequest()
        assertEquals("/users/verify_token", initPost.path)
        assertEquals("POST", initPost.method)
        assertEquals("application/json; utf-8", initPost.getHeader("Content-Type"))
        assertEquals("application/json", initPost.getHeader("Accept"))
        val expectedBody = """{"client_id":"clientId","token":"fail_auth","locale":"fr"}"""
        assertEquals(expectedBody, initPost.body.readUtf8())

        assertTrue(launchCompletion.latch.await(5, TimeUnit.SECONDS))

        verify {
            onLaunchCompletion.onResult(
                match {
                    it is Result.Failure && it.error is LaunchError.InvalidCredentials
                }
            )
        }
        confirmVerified(onLaunchCompletion)
        server.shutdown()
    }

    @Test
    fun launchServerError() {
        val server = MockWebServer()
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        server.enqueue(MockResponse().setResponseCode(500))

        val launchCompletion = LaunchCompletionWithLatch()
        val onLaunchCompletion = spyk(launchCompletion)

        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.launch(Credentials("internal_error"), locale = Locale("sl", "IT", "nedis"), onLaunchCompletion)
        }

        val initPost = server.takeRequest()
        assertEquals("/users/verify_token", initPost.path)
        assertEquals("POST", initPost.method)
        assertEquals("application/json; utf-8", initPost.getHeader("Content-Type"))
        assertEquals("application/json", initPost.getHeader("Accept"))
        val expectedBody = """{"client_id":"clientId","token":"internal_error","locale":"sl-IT-nedis"}"""
        assertEquals(expectedBody, initPost.body.readUtf8())

        assertTrue(launchCompletion.latch.await(5, TimeUnit.SECONDS))

        verify {
            onLaunchCompletion.onResult(
                match {
                    it is Result.Failure && it.error.let { e -> e is LaunchError.HttpError && e.responseCode == 500 }
                }
            )
        }
        confirmVerified(onLaunchCompletion)
        server.shutdown()
    }

    @Test
    fun updateLocale() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)
        activityRule.scenario.onActivity {
            val dreamsView = it.findViewById<DreamsView>(R.id.dreams)
            dreamsView.launch(Credentials("token"), Locale.ROOT)
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
        server.shutdown()
    }

    @Test
    fun updateIdToken() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("expire_token_button") { event, view ->
            assertEquals(Event.CredentialsExpired("uuid"), event)
            view.updateCredentials((event as Event.CredentialsExpired).requestId, Credentials("new token"))
            GlobalScope.launch {
                delay(250)
                latch.countDown()
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        server.shutdown()
    }

    @Test
    fun accountProvisioned() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

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
        server.shutdown()
    }

    @Test
    fun onExitRequested() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("exit_button") { event, _ ->
            assertEquals(Event.ExitRequested, event)
            latch.countDown()
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        server.shutdown()
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

    @Test
    fun onShare() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("share_button") { event, _ ->
            assertEquals(Event.Share, event)
            latch.countDown()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))

        val shareText = "text for share\n" + "http://test.url"
        val shareIntent = allOf(
            hasExtra(equalTo(Intent.EXTRA_TEXT), equalTo(shareText)),
            hasExtra(equalTo(Intent.EXTRA_TITLE), equalTo("testTitle")),
            hasType("text/plain"),
            hasAction(Intent.ACTION_SEND),
        )
        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(equalTo(Intent.EXTRA_INTENT), shareIntent),
            )
        )
        server.shutdown()
    }

    @Test
    fun onShareNullTitle() {
        val server = MockWebServer()
        server.dispatcher = MockDreamsDispatcher(server)
        server.start()
        Dreams.configure(Dreams.Configuration("clientId", server.url("/").toString()))

        val latch = CountDownLatch(1)
        activityRule.testResponseEvent("share_button_null") { event, _ ->
            assertEquals(Event.Share, event)
            latch.countDown()
        }

        val shareText = "text for share\n" + "http://test.url"
        val shareIntent = allOf(
            hasExtra(equalTo(Intent.EXTRA_TEXT), equalTo(shareText)),
            not(hasExtra(equalTo(Intent.EXTRA_TITLE), equalTo(anything()))),
            hasType("text/plain"),
            hasAction(Intent.ACTION_SEND),
        )
        intended(
            allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(equalTo(Intent.EXTRA_INTENT), shareIntent),
            )
        )

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        server.shutdown()
    }
}
