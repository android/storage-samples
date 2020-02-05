/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.samples.filemanager

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.android.samples.filemanager.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, getInfoList())
        binding.infoList.adapter = adapter

        binding.openSettingsButton.setOnClickListener {
            openPermissionSettings(this)
        }
        binding.requestPermissionButton.setOnClickListener {
            requestStoragePermission(this)
        }
    }

    private fun getInfoList(): List<String> {
        return listOf(
            getString(R.string.sdk_codename_info, Build.VERSION.CODENAME),
            getString(R.string.sdk_version_info, Build.VERSION.SDK_INT.toString()),
            getString(R.string.legacy_storage_info, getLegacyStorageStatus()),
            getString(R.string.permission_used_info, getStoragePermissionName()),
            getString(R.string.permission_granted_info, getPermissionStatus(this))
        )
    }
}