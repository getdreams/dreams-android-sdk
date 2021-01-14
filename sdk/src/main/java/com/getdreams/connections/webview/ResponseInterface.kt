/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections.webview

/**
 * Interface for responses from the Dreams web app.
 */
internal interface ResponseInterface {
    /**
     * The token expired.
     */
    fun onIdTokenDidExpire(requestData: String)

    /**
     * A telemetry event was sent.
     *
     * @param data A JSON-string with the telemetry data.
     */
    fun onTelemetryEvent(data: String)

    /**
     * An account should be provisioned.
     */
    fun onAccountProvisionRequested(requestData: String)

    /**
     * The user requested to exit Dreams.
     */
    fun onExitRequested()
}
