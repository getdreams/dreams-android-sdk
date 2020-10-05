/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.getdreams.views.DreamsView

class MainActivity : AppCompatActivity() {
    private val dreamsView: DreamsView by lazy { findViewById(R.id.dreams) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dreamsView.open("token")
    }

    override fun onBackPressed() {
        if (dreamsView.canGoBack()) {
            dreamsView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
