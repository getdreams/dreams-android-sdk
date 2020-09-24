/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.android.sdk

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class DreamsUnitTest {

    @Before
    fun setup() {
        Dreams._instance = null
    }

    @Test
    fun initialize() {
        assertFalse(Dreams.initialized)
        Dreams.setup("clientId", "endpoint")
        assertEquals("clientId", Dreams.instance.clientId)
        assertEquals(Uri.parse("endpoint"), Dreams.instance.baseUri)
        assertTrue(Dreams.initialized)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setupRequiresValidClientId() {
        Dreams.setup("", "url")
    }

    @Test(expected = IllegalArgumentException::class)
    fun setupRequiresValidBaseUrl() {
        Dreams.setup("clientId", "")
    }

    @Test(expected = RuntimeException::class)
    fun getBaseUriThrowsBeforeInitialize() {
        assertFalse(Dreams.initialized)
        Dreams.instance.baseUri
    }

    @Test(expected = RuntimeException::class)
    fun getClientIdThrowsBeforeInitialize() {
        assertFalse(Dreams.initialized)
        Dreams.instance.clientId
    }
}
