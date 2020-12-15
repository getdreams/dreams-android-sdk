/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.test.utils

import android.content.res.AssetManager
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream

/**
 * Get a input stream given a path to a resource file.
 *
 * @param path The path to the file.
 * @throws NullPointerException If file was not found.
 */
fun getInputStreamFromAssets(path: String, accessMode: Int = AssetManager.ACCESS_STREAMING): InputStream {
    return InstrumentationRegistry.getInstrumentation().context.assets.open(path, accessMode)
}
