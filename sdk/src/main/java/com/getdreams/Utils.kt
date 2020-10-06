/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

@file:JvmName("DreamsUtils")

package com.getdreams

import java.util.Locale

/**
 * POSIX formatted locale string.
 *
 * Reference: [GNU manual](https://www.gnu.org/software/gettext/manual/html_node/Locale-Names.html)
 */
val Locale.posix: String
    get() {
        return when {
            country.isEmpty() -> {
                language
            }
            variant.isNotEmpty() -> {
                "${language}_$country@$variant"
            }
            else -> {
                "${language}_${country}"
            }
        }
    }
