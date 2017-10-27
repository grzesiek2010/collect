/*
 * Copyright 2016 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.webkit.WebSettings

import org.odk.collect.android.R

import kotlinx.android.synthetic.main.activity_open_source_licenses.*

class OpenSourceLicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_source_licenses)

        initToolbar()

        web_view_open_source_licenses.settings.loadWithOverviewMode = true
        web_view_open_source_licenses.settings.useWideViewPort = true
        web_view_open_source_licenses.settings.textSize = WebSettings.TextSize.LARGEST
        web_view_open_source_licenses.loadUrl(LICENSES_HTML_PATH)
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        title = getString(R.string.all_open_source_licenses)
        setSupportActionBar(toolbar)
    }

    companion object {
        private val LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html"
    }
}
