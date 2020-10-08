/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.getdreams.Dreams
import com.getdreams.connections.EventListener
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class DreamsViewUnitTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        Dreams.setup("clientId", "https://getdreams.com")
    }

    @Test
    fun canRegisterResponseListener() {
        val dreamsView = DreamsView(context)
        val listener = EventListener { _, _ -> TODO("Not yet implemented") }
        assertTrue(dreamsView.registerEventListener(listener))
    }

    @Test
    fun canRemoveResponseListener() {
        val dreamsView = DreamsView(context)
        val listener = EventListener { _, _ -> TODO("Not yet implemented") }
        dreamsView.registerEventListener(listener)
        assertTrue(dreamsView.removeEventListener(listener))
    }

    @Test
    fun canClearResponseListeners() {
        val dreamsView = DreamsView(context)
        val listener = EventListener { _, _ -> TODO("Not yet implemented") }
        val listener2 = EventListener { _, _ -> TODO("Not yet implemented") }
        dreamsView.registerEventListener(listener)
        dreamsView.registerEventListener(listener2)
        dreamsView.clearEventListeners()
        assertFalse(dreamsView.removeEventListener(listener))
        assertFalse(dreamsView.removeEventListener(listener2))
    }
}
