/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import com.getdreams.views.DreamsView

class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val dreamsView = DreamsView(this@TestActivity)
            dreamsView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            dreamsView.id = R.id.dreams
            addView(dreamsView)
        }
        setContentView(layout)
    }
}
