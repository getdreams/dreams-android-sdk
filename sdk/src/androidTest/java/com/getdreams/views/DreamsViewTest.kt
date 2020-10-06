/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import android.app.Activity
import android.content.Context
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.getdreams.Dreams
import com.getdreams.Location
import org.junit.Test
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@LargeTest
class DreamsViewTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(Activity::class.java)
    private lateinit var context: Context

    @Before
    fun setup() {
        // TODO setup mock web server
        Dreams.setup("clientId", "https://getdreams.com")
    }

    @Test
    fun open() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.open("", Location.Dreams, Locale.CANADA_FRENCH)
        }
    }

    @Test
    fun updateLocale() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.updateLocale(Locale.ROOT)
        }
    }

    @Test
    fun updateAccessToken() {
        activityRule.scenario.onActivity {
            val dreamsView = DreamsView(it)
            dreamsView.updateAccessToken("new_token")
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
