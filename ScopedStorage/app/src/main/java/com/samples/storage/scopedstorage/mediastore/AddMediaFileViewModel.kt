/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samples.storage.scopedstorage.mediastore

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.MediaStoreUtils
import com.samples.storage.scopedstorage.common.MediaStoreUtils.scanUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AddMediaFileViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        private val TAG = this::class.java.simpleName
        const val ADDED_MEDIA_KEY = "addedMedia"
    }

    private val context: Context
        get() = getApplication()

    val canWriteInMediaStore: Boolean
        get() = MediaStoreUtils.canWriteInMediaStore(context)

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    /**
     * We keep the current media [Uri] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    val addedMedia: LiveData<FileResource?> =
        savedStateHandle.getLiveData<FileResource?>(ADDED_MEDIA_KEY)

    fun addImage() {
        viewModelScope.launch {
            val filename = "added-${System.currentTimeMillis()}.jpg"

            val uri = MediaStoreUtils.createImageUri(context, filename)
                ?: return@launch _errorFlow.emit("Couldn't create an image Uri\n$filename")

            try {
                context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                    context.assets.open("sample.jpg").use { inputStream ->
                        inputStream.copyTo(outputStream)
                        scanUri(context, uri, "image/jpg")
                        savedStateHandle[ADDED_MEDIA_KEY] =
                            MediaStoreUtils.getResourceByUri(context, uri)
                    }
                }

            } catch (e: IOException) {
                Log.e(TAG, e.printStackTrace().toString())
                _errorFlow.emit("Couldn't save the image\n$uri")
            }
        }
    }

    fun addVideo() {
        viewModelScope.launch {
            val filename = "added-${System.currentTimeMillis()}.mp4"

            val uri = MediaStoreUtils.createVideoUri(context, filename)
                ?: return@launch _errorFlow.emit("Couldn't create an video Uri\n$filename")

            try {
                context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                    context.assets.open("sample.mp4").use { inputStream ->
                        inputStream.copyTo(outputStream)
                        scanUri(context, uri, "video/mp4")
                        savedStateHandle[ADDED_MEDIA_KEY] =
                            MediaStoreUtils.getResourceByUri(context, uri)
                    }
                }

            } catch (e: IOException) {
                Log.e(TAG, e.printStackTrace().toString())
                _errorFlow.emit("Couldn't save the video\n$uri")
            }
        }
    }
}