/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.test.utils

import android.content.res.AssetManager
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.getdreams.Credentials
import com.getdreams.R
import com.getdreams.TestActivity
import com.getdreams.events.Event
import com.getdreams.views.DreamsView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Get a input stream given a path to a resource file.
 *
 * @param path The path to the file.
 * @throws NullPointerException If file was not found.
 */
fun getInputStreamFromAssets(path: String, accessMode: Int = AssetManager.ACCESS_STREAMING): InputStream {
    return InstrumentationRegistry.getInstrumentation().context.assets.open(path, accessMode)
}

/**
 * Opens and wait for page to be loaded before clicking on [buttonId] that should trigger an event for [listener].
 */
inline fun ActivityScenarioRule<TestActivity>.testResponseEvent(
    buttonId: String,
    crossinline listener: (Event, DreamsView) -> Unit
) {
    val contentLatch = CountDownLatch(1)
    scenario.onActivity { activity ->
        val dreamsView = activity.findViewById<DreamsView>(R.id.dreams)
        dreamsView.launch(Credentials("token"))
        dreamsView.registerEventListener { event ->
            when (event) {
                is Event.Telemetry -> {
                    if ("content_loaded" == event.name) {
                        contentLatch.countDown()
                        dreamsView.clearEventListeners()
                        dreamsView.registerEventListener {
                            // Run on main to register errors for test
                            GlobalScope.launch(Dispatchers.Main) {
                                listener(it, dreamsView)
                            }
                        }
                    } else {
                        throw IllegalStateException("Got other event before content_loaded")
                    }
                }
                else -> throw IllegalStateException("Got other event before content_loaded")
            }
        }
    }
    assertTrue(contentLatch.await(5, TimeUnit.SECONDS))
    onWebView()
        .withElement(findElement(Locator.ID, buttonId))
        .perform(webClick())
}
