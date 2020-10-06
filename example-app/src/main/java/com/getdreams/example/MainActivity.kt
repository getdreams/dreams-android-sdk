/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.getdreams.connections.ResponseListener
import com.getdreams.events.Event
import com.getdreams.views.DreamsView

class MainActivity : AppCompatActivity() {
    private val dreamsView: DreamsView by lazy { findViewById(R.id.dreams) }

    private val listener = ResponseListener { type, _ ->
        when (type) {
            Event.Response.Initialized -> {
                // no-op
            }
            Event.Response.AccessTokenExpired -> {
                dreamsView.updateAccessToken("new_token")
            }
            Event.Response.OffboardingCompleted -> {
                this@MainActivity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dreamsView.open("token")
        dreamsView.registerResponseListener(listener)
    }

    override fun onDestroy() {
        dreamsView.removeResponseListener(listener)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (dreamsView.canGoBack()) {
            dreamsView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
