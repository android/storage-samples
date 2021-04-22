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
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * URL returning random picture provided by Unsplash. Read more here: https://source.unsplash.com
 */
private const val RANDOM_IMAGE_URL = "https://source.unsplash.com/random/500x500"

class AddMediaViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val context: Context
        get() = getApplication()

    val canWriteInMediaStore: Boolean
        get() = checkMediaStorePermission(context)

    /**
     * Using lazy to instantiate the [OkHttpClient] only when accessing it, not when the viewmodel
     * is created
     */
    private val httpClient by lazy { OkHttpClient() }

    /**
     * We keep the current media [Uri] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    val currentMediaUri: LiveData<Uri?> = savedStateHandle.getLiveData<Uri?>("currentMediaUri")

    /**
     * TakePicture activityResult action isn't returning the [Uri] once the image has been taken, so
     * we need to save the temporarily created URI in [savedStateHandle] until we handle its result
     */
    fun saveTemporarilyPhotoUri(uri: Uri?) {
        savedStateHandle["temporaryPhotoUri"] = uri
    }

    val temporaryPhotoUri: Uri?
        get() = savedStateHandle.get<Uri?>("temporaryPhotoUri")

    /**
     * [loadCameraMedia] is called when TakePicture or TakeVideo intent is returning a
     * successful result, that we set to the currentMediaUri property, which will
     * trigger to load the thumbnail in the UI
     */
    fun loadCameraMedia(uri: Uri) {
        savedStateHandle["currentMediaUri"] = uri
    }

    /**
     * We create a [Uri] where the image will be stored
     */
    suspend fun createPhotoUri(source: Source): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newImage = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, generateFilename(source, "jpg"))
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(imageCollection, newImage)
        }
    }

    /**
     * We create a [Uri] where the camera will store the video
     */
    suspend fun createVideoUri(source: Source): Uri? {
        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return withContext(Dispatchers.IO) {
            val newVideo = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, generateFilename(source, "mp4"))
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(videoCollection, newVideo)
        }
    }

    /**
     * [saveRandomImageFromInternet] downloads a random image from unsplash.com and saves its
     * content
     */
    fun saveRandomImageFromInternet(callback: () -> Unit) {
        viewModelScope.launch {
            val imageUri = createPhotoUri(Source.INTERNET)
            // We use OkHttp to create HTTP request
            val request = Request.Builder().url(RANDOM_IMAGE_URL).build()

            withContext(Dispatchers.IO) {

                imageUri?.let { destinationUri ->
                    val response = httpClient.newCall(request).execute()

                    // .use is an extension function that closes the output stream where we're
                    // saving the image content once its lambda is finished being executed
                    response.body?.use { responseBody ->
                        context.contentResolver.openOutputStream(destinationUri, "w")?.use {
                            responseBody.byteStream().copyTo(it)

                            /**
                             * We can't set savedStateHandle within a background thread, so we do it
                             * within the [Dispatchers.Main], which execute its coroutines on the
                             * main thread
                             */
                            withContext(Dispatchers.Main) {
                                savedStateHandle["currentMediaUri"] = destinationUri
                                callback()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Check if the app can writes on the shared storage
 *
 * On Android 10 (API 29), we can add media to MediaStore without having to request the
 * [WRITE_EXTERNAL_STORAGE] permission, so we only check on pre-API 29 devices
 */
private fun checkMediaStorePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

enum class Source {
    CAMERA, INTERNET
}

private fun generateFilename(source: Source, extension: String): String {
    return when (source) {
        Source.CAMERA -> {
            "camera-${System.currentTimeMillis()}.$extension"
        }
        Source.INTERNET -> {
            "internet-${System.currentTimeMillis()}.$extension"
        }
    }
}
