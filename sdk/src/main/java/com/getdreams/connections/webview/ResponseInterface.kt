/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections.webview

/**
 * Interface for communication from the Dreams PWA.
 */
internal interface ResponseInterface {

    /**
     * Access token expired.
     */
    fun onAccessTokenDidExpire()

    /**
     * User offboarded.
     */
    fun onOffboardingDidComplete()
}
