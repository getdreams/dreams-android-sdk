/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections.webview

import com.getdreams.Location
import java.util.Locale

/**
 * Interface for requests to the Dreams PWA.
 */
interface RequestInterface {
    /**
     * Open Dreams at [location].
     *
     * @param idToken The token used to authenticate.
     * @param location What screen to open, by default `home`.
     */
    fun open(idToken: String, location: String = Location.Home.value, locale: Locale? = null)

    /**
     * Set the locale used in Dreams.
     *
     * @param locale The new locale.
     */
    fun updateLocale(locale: Locale)

    /**
     * Update the id token.
     *
     * @param requestId The request id of the event that informed that the token was expired.
     * @param idToken The new id token.
     */
    fun updateIdToken(requestId: String, idToken: String)

    /**
     * Inform the PWA that an account was provisioned.
     *
     * @param requestId The request id of the event that triggered the account provisioning.
     */
    fun accountProvisionInitiated(requestId: String)
}
