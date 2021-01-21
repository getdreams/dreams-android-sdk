/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import com.getdreams.connections.EventListener
import com.getdreams.connections.webview.RequestInterface

/**
 * Interface for [DreamsView].
 */
interface DreamsViewInterface : RequestInterface {
    /**
     * Listen to events from Dreams.
     *
     * @param listener The listener to register.
     *
     * @return If [listener] was registered successfully.
     */
    fun registerEventListener(listener: EventListener): Boolean

    /**
     * Remove a registered listener.
     *
     * @param listener The listener to remove.
     *
     * @return If the [listener] was removed.
     */
    fun removeEventListener(listener: EventListener): Boolean

    /**
     * Remove all event listeners.
     */
    fun clearEventListeners()

    /**
     * Returns true if the dreams view can navigate back.
     */
    fun canGoBack(): Boolean

    /**
     * Navigates back in the dreams view.
     */
    fun goBack()
}
