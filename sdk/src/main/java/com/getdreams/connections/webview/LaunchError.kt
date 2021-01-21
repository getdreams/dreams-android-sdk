/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.connections.webview

/**
 * This class represents an error when trying to launch a Dreams view.
 *
 * @param message The detail message string.
 * @param cause The cause of the error.
 */
sealed class LaunchError(val message: String, val cause: Throwable?) {
    /**
     * The supplied credentials were invalid.
     */
    class InvalidCredentials(message: String, cause: Throwable?) : LaunchError(message, cause)

    /**
     * There was an HTTP error.
     *
     * @param responseCode The HTTP response code.
     */
    class HttpError(val responseCode: Int, message: String, cause: Throwable?) : LaunchError(message, cause)

    /**
     * A unexpected error was encountered.
     */
    class UnexpectedError(message: String, cause: Throwable) : LaunchError(message, cause)
}
