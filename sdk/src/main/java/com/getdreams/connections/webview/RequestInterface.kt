/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections.webview

import com.getdreams.Location
import com.getdreams.Credentials
import java.util.Locale

/**
 * Interface for requests to the Dreams web app.
 */
interface RequestInterface {
    /**
     * Launch Dreams at [location].
     *
     * @param credentials Credentials used to authenticate the user.
     * @param location What screen to open, by default `home`.
     * @param locale If set overrides the user locale.
     */
    fun launch(credentials: Credentials, location: String = Location.Home.value, locale: Locale? = null)

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
     * @param credentials The new credentials to use.
     */
    fun updateCredentials(requestId: String, credentials: Credentials)

    /**
     * Inform the web app that an account was provisioned.
     *
     * @param requestId The request id of the event that triggered the account provisioning.
     */
    fun accountProvisionInitiated(requestId: String)
}
