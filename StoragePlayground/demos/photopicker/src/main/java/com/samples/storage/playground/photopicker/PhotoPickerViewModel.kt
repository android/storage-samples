/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.samples.storage.playground.photopicker

import android.annotation.SuppressLint
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PhotoPickerViewModel : ViewModel() {
    companion object {
        val MAX_ITEMS_VALUES = setOf(1, 3, 5, 10, getMaxItemsLimit())

        @SuppressLint("NewApi")
        private fun getMaxItemsLimit(): Int {
            return if (isPhotoPickerAvailable()) {
                MediaStore.getPickImagesMaxLimit()
            } else {
                100
            }
        }
    }

    enum class FileTypeFilter {
        ImageAndVideo, Image, Video
    }

    data class UiState(
        val availablePicker: String,
        val maxItems: Int,
        val fileTypeFilter: FileTypeFilter = FileTypeFilter.ImageAndVideo,
        val items: List<Uri> = emptyList()
    )

    var uiState by mutableStateOf(
        UiState(
            availablePicker = if (isPhotoPickerAvailable()) "Photo Picker" else "Document Picker",
            maxItems = 5
        )
    )
        private set

    fun getMaxItems(): Int {
        return uiState.maxItems
    }

    fun onFileTypeFilterChange(fileTypeFilter: FileTypeFilter) {
        uiState = uiState.copy(fileTypeFilter = fileTypeFilter)
    }

    fun onMaxItemsChange(maxItems: Int) {
        uiState = uiState.copy(maxItems = maxItems)
    }

    fun onSingleSelect(uri: Uri?) {
        uri?.let {
            uiState = uiState.copy(items = listOf(it))
        }
    }

    fun onMultipleSelect(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            uiState = uiState.copy(items = uris)
        }
    }

    fun createPickVisualMediaRequest(): PickVisualMediaRequest {
        return when (uiState.fileTypeFilter) {
            FileTypeFilter.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
            FileTypeFilter.Image -> PickVisualMediaRequest(ImageOnly)
            FileTypeFilter.Video -> PickVisualMediaRequest(VideoOnly)
        }
    }
}