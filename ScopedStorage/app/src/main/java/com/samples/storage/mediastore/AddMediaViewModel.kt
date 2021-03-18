/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.samples.storage.mediastore

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddMediaViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication()

    val isPermissionGranted: Boolean
        get() = canWriteInMediaStore(context)

    // We keep the current Media in the viewmodel to re-render it if there is a configuration change
    private val _currentMedia: MutableStateFlow<Media?> = MutableStateFlow(null)
    val currentMedia: StateFlow<Media?> = _currentMedia

    fun loadMedia() {
        // Once we get a result from [TakePicture] and [TakeVideo], we clear the value of the
        // temporaryMediaUri property
        _temporaryMediaUri.value = null
    }

    /**
     * [TakePicture] and [TakeVideo] activityResult actions isn't returning the URI once it's
     * returning the result, so we need to keep the temporarily created URI until the action is
     * finished
     */
    private val _temporaryMediaUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val temporaryMediaUri: StateFlow<Uri?> = _temporaryMediaUri

    // We create a URI where the camera will store the image
    fun createPhotoUri(source: Source): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val newImage = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, generateFilename(source, "jpg"))
        }

        val uri = context.contentResolver.insert(imageCollection, newImage)
        _temporaryMediaUri.value = uri

        return uri
    }

    // We create a URI where the camera will store the video
    fun createVideoUri(): Uri? {
        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val newVideo = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, generateFilename(Source.CAMERA, "mp4"))
        }

        val uri = context.contentResolver.insert(videoCollection, newVideo)
        _temporaryMediaUri.value = uri

        return uri
    }
}

private fun canWriteInMediaStore(context: Context): Boolean {
    // On Android 10 (API 29), we can add media to MediaStore without having to request the
    // WRITE_EXTERNAL_STORAGE permission, so we only check on pre-API 29 devices
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

enum class Source {
    CAMERA, INTERNET, GENERATED
}

private fun generateFilename(source: Source, extension: String): String {
    return when (source) {
        Source.CAMERA -> {
            "camera-${System.currentTimeMillis()}.$extension"
        }
        Source.INTERNET -> {
            "internet-${System.currentTimeMillis()}.$extension"
        }
        Source.GENERATED -> {
            "generated-${System.currentTimeMillis()}.$extension"
        }
    }
}

data class Media(val filename: String, val size: Long, val uri: Uri)
