/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.getdreams.connections.EventListener
import com.getdreams.events.Event
import com.getdreams.views.DreamsView

class MainActivity : AppCompatActivity() {
    private val dreamsView: DreamsView by lazy { findViewById(R.id.dreams) }

    private val listener = EventListener { event ->
        when (event) {
            is Event.IdTokenExpired -> {
                dreamsView.updateIdToken(requestId = event.requestId, idToken = "new_token")
            }
            is Event.Telemetry -> {
                Log.v("MainActivity", "Got telemetry ${event.name}: ${event.payload?.toString(2)}")
            }
            is Event.AccountProvisionRequested -> {
                // Provision an account to the user
                AlertDialog.Builder(this)
                    .setMessage("Provision Account?")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // Tell Dreams the account was provisioned
                        dreamsView.accountProvisioned(requestId = event.requestId)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }
            is Event.ExitRequested -> {
                // We should exit the Dreams context
                finish()
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
