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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.MediaStoreUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CaptureMediaFileViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        private val TAG = this::class.java.simpleName
        const val CAPTURED_MEDIA_KEY = "capturedMedia"
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
    val capturedMedia: LiveData<FileResource?> =
        savedStateHandle.getLiveData<FileResource?>(CAPTURED_MEDIA_KEY)

    suspend fun createImageUri(): Uri? {
        val filename = "camera-${System.currentTimeMillis()}.jpg"
        val uri = MediaStoreUtils.createImageUri(context, filename)

        return if (uri != null) {
            uri
        } else {
            _errorFlow.emit("Couldn't create an image Uri\n$filename")
            null
        }
    }

    suspend fun createVideoUri(): Uri? {
        val filename = "camera-${System.currentTimeMillis()}.mp4"
        val uri = MediaStoreUtils.createVideoUri(context, filename)

        return if (uri != null) {
            uri
        } else {
            _errorFlow.emit("Couldn't create a video Uri\n$filename")
            null
        }
    }

    fun onImageCapture(uri: Uri) {
        viewModelScope.launch {
            MediaStoreUtils.scanUri(context, uri, "image/jpg")
            savedStateHandle[CAPTURED_MEDIA_KEY] = MediaStoreUtils.getResourceByUri(context, uri)
        }
    }

    fun onVideoCapture(uri: Uri) {
        viewModelScope.launch {
            MediaStoreUtils.scanUri(context, uri, "video/mp4")
            savedStateHandle[CAPTURED_MEDIA_KEY] = MediaStoreUtils.getResourceByUri(context, uri)
        }
    }
}