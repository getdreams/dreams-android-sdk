/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.events

/**
 * Interface detailing what data comes with all requests from the PWA.
 */
interface RequestData {
    /**
     * An id to keep track of request -> response flows.
     */
    val requestId: String
}
