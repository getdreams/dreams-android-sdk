/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.events

@Suppress("unused")
enum class RequestType {
    UpdateAccessToken,
    UpdateLocale,
}

@Suppress("unused")
enum class ResponseType {
    AccessTokenExpired,
    OffboardingCompleted,
}

sealed class Event {
    data class Request(val type: RequestType): Event()
    data class Response(val type: ResponseType): Event()
}
