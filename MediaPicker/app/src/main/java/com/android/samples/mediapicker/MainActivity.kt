/*
 *
 *  * Copyright (C) 2020 The Android Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.android.samples.mediapicker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.android.samples.mediapicker.databinding.ActivityMainBinding

const val IMAGE_MIME_TYPE = "image/*"
const val VIDEO_MIME_TYPE = "video/*"
const val AUDIO_MIME_TYPE = "audio/*"

const val FILE_PICKER_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pickFileButton.setOnClickListener {
            showMenu(binding.pickFileButton)
        }
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.media_types_menu, popup.menu)

        popup.setOnMenuItemClickListener {
            pickFile(it.itemId)
            true
        }

        popup.show()
    }

    private fun pickFile(@IdRes type: Int) {
        val mimeType = when (type) {
            R.id.imageType -> IMAGE_MIME_TYPE
            R.id.videoType -> VIDEO_MIME_TYPE
            R.id.audioType -> AUDIO_MIME_TYPE
            else -> IMAGE_MIME_TYPE
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = mimeType
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            println("GOTCHA")
        }
    }
}
