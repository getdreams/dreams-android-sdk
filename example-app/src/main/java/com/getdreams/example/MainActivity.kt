/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.getdreams.connections.EventListener
import com.getdreams.events.Event
import com.getdreams.views.DreamsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val dreamsView: DreamsView
        get() = findViewById(R.id.dreams)

    /**
     * This is the main event listener.
     * In this example it handles all events from Dreams, but you could split this into several listeners if needed.
     */
    private val listener = EventListener { event ->
        when (event) {
            is Event.IdTokenExpired -> {
                // Renew the token
                GlobalScope.launch {
                    val newToken = FakeBackend.refreshIdToken()
                    dreamsView.updateIdToken(requestId = event.requestId, idToken = newToken)
                }
            }
            is Event.Telemetry -> {
                // Convert from JSONObject to Map
                val params = event.payload?.keys()?.asSequence()?.associate { it to event.payload?.get(it) }
                // Pass the event on
                FakeBackend.sendAnalyticsEvent(event.name, params)
            }
            is Event.AccountProvisionRequested -> {
                // Provision an account to the user
                GlobalScope.launch {
                    if (FakeBackend.provisionAccountAsync(this@MainActivity).await()) {
                        // Tell Dreams the account was provisioned
                        dreamsView.accountProvisionInitiated(requestId = event.requestId)
                    } else {
                        // Something went wrong with provisioning the account
                        Toast.makeText(
                            this@MainActivity,
                            "Could not provision account",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            is Event.ExitRequested -> {
                // We should exit the Dreams context
                this@MainActivity.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dreamsView.open("token")
        dreamsView.registerEventListener(listener)
    }

    override fun onDestroy() {
        // As we are about to be destroyed we no longer need to listen for events from Dreams.
        dreamsView.removeEventListener(listener)
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
