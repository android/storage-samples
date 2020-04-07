/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.samples.safdemos.mediapicker

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.android.samples.safdemos.R
import com.android.samples.safdemos.databinding.FragmentMediaPickerBinding

const val IMAGE_MIME_TYPE = "image/*"
const val VIDEO_MIME_TYPE = "video/*"
const val AUDIO_MIME_TYPE = "audio/*"

const val FILE_PICKER_REQUEST_CODE = 1

class MediaPickerFragment : Fragment() {
    private lateinit var binding: FragmentMediaPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMediaPickerBinding.inflate(layoutInflater)

        binding.pickFileButton.setOnClickListener {
            showMenu(binding.pickFileButton)
        }

        return binding.root
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(activity!!.baseContext, anchor)
        popup.menuInflater.inflate(R.menu.media_picker_media_types, popup.menu)

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

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            setType(mimeType)
        }

        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                activity?.let {
                    binding.preview.setImageBitmap(
                        BitmapFactory.decodeStream(it.contentResolver.openInputStream(uri))
                    )
                }
            }
        }
    }
}
