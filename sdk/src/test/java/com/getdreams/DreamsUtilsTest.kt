/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@SmallTest
class DreamsUtilsUnitTest {

    @Test
    fun locale_getPosix() {
        assertEquals("fr_CA", Locale.CANADA_FRENCH.posix)
        assertEquals("de", Locale.GERMAN.posix)
        assertEquals("", Locale.ROOT.posix)
        assertEquals("fi_FI@euro", Locale("fi", "FI", "euro").posix)
    }
}
