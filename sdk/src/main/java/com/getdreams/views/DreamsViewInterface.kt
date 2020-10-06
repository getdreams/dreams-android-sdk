/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.views

import com.getdreams.Location
import com.getdreams.connections.ResponseListener
import java.util.Locale

/**
 * Interface for [DreamsView].
 */
interface DreamsViewInterface {
    /**
     * Open Dreams at [location].
     *
     * @param accessToken The token used to authenticate.
     * @param location What screen to open, by default [Location.Home].
     */
    fun open(accessToken: String, location: Location = Location.Home, locale: Locale? = null)

    /**
     * Set the locale used in Dreams.
     */
    fun updateLocale(locale: Locale)

    /**
     * Update the access token.
     */
    fun updateAccessToken(accessToken: String)

    /**
     * Listen to events from Dreams.
     * Note that listeners are held with a strong reference, so make sure to remove them with [removeResponseListener].
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

    /**
     * Returns true if the dreams view can navigate back.
     */
    fun canGoBack(): Boolean

    /**
     * Navigates back in the dreams view.
     */
    fun goBack()
}
