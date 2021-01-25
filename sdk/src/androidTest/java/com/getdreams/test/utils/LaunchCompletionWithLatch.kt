/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.test.utils

import com.getdreams.Result
import com.getdreams.connections.webview.LaunchError
import com.getdreams.connections.webview.RequestInterface
import java.util.concurrent.CountDownLatch

/**
 * A convenience class for having a launch completion with a latch.
 *
 * This is mostly used because of [this mockk issue](https://github.com/mockk/mockk/issues/258).
 */
open class LaunchCompletionWithLatch : RequestInterface.OnLaunchCompletion {
    val latch = CountDownLatch(1)
    override fun onResult(result: Result<Unit, LaunchError>) {
        latch.countDown()
    }
}
