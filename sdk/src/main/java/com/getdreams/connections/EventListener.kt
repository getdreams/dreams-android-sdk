/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections

import com.getdreams.events.Event

/**
 * Listener for events from Dreams.
 */
fun interface EventListener {
    /**
     * Triggered when a [Event] is sent from PWA.
     *
     * @param event The event that was sent.
     */
    fun onEvent(event: Event)
}
