/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import android.app.Application
import com.getdreams.Dreams

@Suppress("unused")
class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Dreams.setup(clientId = "clientId", baseUrl = "https://getdreams.io")
    }
}
