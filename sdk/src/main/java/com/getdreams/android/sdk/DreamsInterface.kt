/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.android.sdk

import com.getdreams.android.sdk.connections.ResponseListener

/**
 * The main interface to Dreams.
 */
interface DreamsInterface {
    /**
     * Listen to events from Dreams.
     *
     * @param listener The listener to register.
     *
     * @return If [listener] was registered successfully.
     */
    fun registerResponseListener(listener: ResponseListener): Boolean

    /**
     * Remove a registered listener.
     *
     * @param listener The listener to remove.
     *
     * @return If the [listener] was removed.
     */
    fun removeResponseListener(listener: ResponseListener): Boolean

    /**
     * Remove all event listeners.
     */
    fun clearResponseListeners()
}
