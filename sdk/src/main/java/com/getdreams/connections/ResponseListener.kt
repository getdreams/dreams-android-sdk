/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections

import com.getdreams.events.Event
import org.json.JSONObject

/**
 * Listener for response events from Dreams.
 */
fun interface ResponseListener {
    /**
     * Triggered when a [Event.Response] is sent from PWA.
     *
     * @param type The type of response.
     * @param data The data of the response.
     */
    fun onResponse(type: Event.Response, data: JSONObject?)
}
