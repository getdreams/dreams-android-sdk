/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams

import android.net.Uri

/**
 * Dreams instance.
 */
class Dreams internal constructor(
    internal val clientId: String,
    baseUrl: String
) {
    /**
     * Dreams singleton object.
     */
    companion object Singleton {
        @Volatile
        internal var _instance: Dreams? = null

        /**
         * Initialize the Dreams SDK. This must be called before calling [com.getdreams.views.DreamsView].
         *
         * @param clientId The client id.
         * @param baseUrl The endpoint that the sdk should use.
         */
        @JvmStatic
        fun setup(clientId: String, baseUrl: String): Dreams {
            require(clientId.isNotEmpty()) { "Client id must be set" }
            require(baseUrl.isNotEmpty()) { "Base url must not be empty" }

            val i = _instance
            if (i != null) {
                return i
            }

            return synchronized(this) {
                val syncInstance = _instance
                if (syncInstance != null) {
                    syncInstance
                } else {
                    val created = Dreams(clientId, baseUrl)
                    _instance = created
                    created
                }
            }
        }

        /**
         * The [Dreams] instance.
         *
         * @throws IllegalStateException If [Dreams.setup] has not been called.
         */
        @JvmStatic
        val instance: Dreams
            get() {
                val i = _instance
                if (i != null) {
                    return i
                }

                return synchronized(this) {
                    val syncInstance = _instance
                    @Suppress("IfThenToElvis")
                    if (syncInstance != null) {
                        syncInstance
                    } else {
                        throw IllegalStateException("Dreams.setup() must be called before getting the instance.")
                    }
                }
            }

        @JvmStatic
        val initialized: Boolean
            get() {
                val i = _instance
                if (i != null) {
                    return true
                }

                return synchronized(this) {
                    val syncInstance = _instance
                    (syncInstance != null)
                }
            }
    }

    /**
     * The base [Uri] to load.
     */
    internal val baseUri: Uri = Uri.parse(baseUrl)
}
