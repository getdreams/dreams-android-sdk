/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay

object FakeBackend {
    /**
     * Pretend to do a async refresh of a token
     */
    suspend fun refreshIdToken(): String {
        return GlobalScope.async {
            // Pretend to negotiate a refresh
            delay(250)
            return@async "a fresh token"
        }.await()
    }

    /**
     * Pretend to send a event
     */
    fun sendAnalyticsEvent(name: String, params: Map<String, Any?>?) {
        Log.v("FakeBackend", "Got event ${name}: $params")
    }

    /**
     * Pretend to provision an account, if the deferred value is true an account was provisioned.
     */
    fun provisionAccountAsync(context: Context): Deferred<Boolean> {
        return GlobalScope.async {
            val result = Job()
            AlertDialog.Builder(context)
                .setMessage("Provision Account?")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    result.complete()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    result.cancel()
                    dialog.cancel()
                }
                .show()
            result.join()
            result.isCompleted
        }
    }
}
