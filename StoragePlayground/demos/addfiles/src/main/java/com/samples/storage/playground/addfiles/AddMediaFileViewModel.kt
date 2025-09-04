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

package com.samples.storage.playground.addfiles

import android.app.Application
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.samples.storage.playground.addfiles.FileUtils.generateFilename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File

class AddMediaFileViewModel(application: Application) : AndroidViewModel(application) {
    private val client = OkHttpClient()
    private val context: Context
        get() = getApplication()

    enum class Status {
        NOT_STARTED, LOADING, SUCCESS, ERROR
    }

    enum class StorageDestination {
        Internal, Shared
    }

    data class MediaMetadata(
        val uri: Uri,
        val filename: String,
        val size: Long,
        val mimeType: String,
        val orientation: String,
        val width: Long,
        val height: Long,
        val durationInMs: Long?
    )

    data class UiState(
        val status: Status = Status.NOT_STARTED,
        val fileType: MediaFile = MediaFile.Image,
        val storageDestination: StorageDestination = StorageDestination.Shared,
        val metadata: MediaMetadata? = null
    )

    var uiState by mutableStateOf(UiState())
        private set

    fun onFileTypeChange(fileType: MediaFile) {
        uiState = uiState.copy(fileType = fileType)
    }

    fun onDestinationChange(storageDestination: StorageDestination) {
        uiState = uiState.copy(storageDestination = storageDestination)
    }

    fun download() {
        uiState = uiState.copy(status = Status.LOADING)
        val state = uiState.copy()

        viewModelScope.launch(Dispatchers.IO) {
            val url = state.fileType.random()
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("AddMediaFileViewModel", "Download failed: $response")
                    withContext(Dispatchers.Main) {
                        uiState = uiState.copy(status = Status.ERROR)
                    }

                    return@launch
                }

                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(status = Status.SUCCESS)
                }

                try {
                    when (state.storageDestination) {
                        StorageDestination.Internal -> saveToInternalStorage(
                            state.fileType,
                            response
                        )
                        StorageDestination.Shared -> saveToSharedStorage(state.fileType, response)
                    }

                    withContext(Dispatchers.Main) {
                        uiState = uiState.copy(status = Status.SUCCESS)
                    }

                } catch (error: Exception) {
                    Log.e("AddMediaFileViewModel", "Copy failed: $error")

                    withContext(Dispatchers.Main) {
                        uiState = uiState.copy(status = Status.ERROR)
                    }
                }
            }
        }
    }

    private fun saveToInternalStorage(fileType: MediaFile, response: Response) {
        response.use {
            response.body?.byteStream()?.use { inputStream ->
                val newFile = File(context.filesDir, generateFilename(fileType.extension))
                newFile.outputStream().use {
                    inputStream.copyTo(it)
                }

                Log.d("AddMediaFileViewModel", "File copied (internal): $newFile")
            }
        }
    }

    private fun saveToSharedStorage(fileType: MediaFile, response: Response) {
        response.use {
            val newFile = File(
                fileType.getSharedFolder(),
                generateFilename(fileType.extension)
            )

            response.body?.byteStream()?.use { inputStream ->
                inputStream.use {
                    newFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(newFile.path),
                arrayOf(fileType.mimeType)
            ) { _, scannedUri ->
                if (scannedUri == null) {
                    Log.e(
                        "AddMediaFileViewModel",
                        "File ${newFile.path} could not be scanned"
                    )
                } else {
                    Log.d(
                        "AddMediaFileViewModel",
                        "File ${newFile.path} scanned: $scannedUri"
                    )
                }
            }
        }
    }

//    suspend fun getResourceByUri(context: Context, uri: Uri): MediaMetadata {
//        return withContext(Dispatchers.IO) {
//            // Convert generic media uri to content uri to get FileColumns.MEDIA_TYPE value
//            val contentUri = convertMediaUriToContentUri(uri)
//
//            val projection = arrayOf(
//                MediaStore.Files.FileColumns._ID,
//                MediaStore.Files.FileColumns.DISPLAY_NAME,
//                MediaStore.Files.FileColumns.SIZE,
//                MediaStore.Files.FileColumns.MEDIA_TYPE,
//                MediaStore.Files.FileColumns.MIME_TYPE,
//                MediaStore.Files.FileColumns.DATA,
//            )
//
//            val cursor = contentUri?.let {
//                context.contentResolver.query(
//                    it,
//                    projection,
//                    null,
//                    null,
//                    null
//                )
//            } ?: throw Exception("Uri $uri could not be found")
//
//            cursor.use {
//                if (!cursor.moveToFirst()) {
//                    throw Exception("Uri $uri could not be found")
//                }
//
//                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
//                val displayNameColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
//                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
//                val mediaTypeColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
//                val mimeTypeColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
//                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
//
//                return MediaMetadata(
//                    uri = uri,
//                    filename = ,
//                    size = ,
//                    mimeType = ,
//                    orientation = ,
//                    width = ,
//                    height = ,
//                    dateTaken = ,
//                    durationInMs = ,
//                )
//
//                FileResource(
//                    uri = contentUri,
//                    filename = cursor.getString(displayNameColumn),
//                    size = cursor.getLong(sizeColumn),
//                    type = FileType.getEnum(cursor.getInt(mediaTypeColumn)),
//                    mimeType = cursor.getString(mimeTypeColumn),
//                    path = cursor.getString(dataColumn),
//                )
//            }
//        }
//    }

    private suspend fun getImageMetadata(uri: Uri): MediaMetadata? {
        return withContext(Dispatchers.IO) {

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.HEIGHT,
                MediaStore.MediaColumns.WIDTH,
                MediaStore.MediaColumns.ORIENTATION,
                MediaStore.MediaColumns.DURATION,
            )

            return@withContext null

//            return MediaMetadata(
//                uri = uri,
//                filename =,
//                size =,
//                mimeType =,
//                orientation =,
//                width =,
//                height =,
//                dateTaken =,
//                durationInMs =,
//            )
        }
    }
}