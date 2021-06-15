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
package com.samples.storage.mediastore

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.data.SampleFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.attribute.FileTime

private const val TAG = "AddDocumentViewModel"

class AddDocumentViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val context: Context
        get() = getApplication()

    /**
     * Check ability to add document in the Download folder or not
     */
    val canAddDocument: Boolean
        get() = canAddDocumentPermission(context)

    /**
     * Using lazy to instantiate the [OkHttpClient] only when accessing it, not when the viewmodel
     * is created
     */
    private val httpClient by lazy { OkHttpClient() }

    /**
     * We keep the current [FileEntry] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    private var _isDownloading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDownloading: LiveData<Boolean> = _isDownloading

    /**
     * We keep the current [FileEntry] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    private var _currentFileEntry: MutableLiveData<FileEntry> = MutableLiveData(null)
    val currentFileEntry: LiveData<FileEntry> = _currentFileEntry

    init {
        val fileEntryBundle = savedStateHandle.get<Bundle>("current_file")
        if (fileEntryBundle != null) {
            _currentFileEntry.value = FileEntry.fromBundle(fileEntryBundle)
        }
        savedStateHandle.setSavedStateProvider("current_file") { // saveState()
            if (_currentFileEntry.value != null) {
                _currentFileEntry.value!!.toBundle()
            } else {
                Bundle()
            }
        }
    }

    /**
     * Generate random filename when saving a new file
     */
    private fun generateFilename(extension: String) = "${System.currentTimeMillis()}.$extension"

    /**
     * Check if the app can writes on the shared storage
     *
     * On Android 10 (API 29), we can add files to the Downloads folder without having to request the
     * [WRITE_EXTERNAL_STORAGE] permission, so we only check on pre-API 29 devices
     */
    private fun canAddDocumentPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun addRandomFile() {
        _isDownloading.postValue(true)

        val randomRemoteUrl = SampleFiles.nonMedia.random()
        val extension = randomRemoteUrl.substring(randomRemoteUrl.lastIndexOf(".") + 1)
        val filename = generateFilename(extension)

        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val uri = addFileToDownloadsApi29(filename)
                    val outputStream = context.contentResolver.openOutputStream(uri, "w")
                        ?: throw Exception("ContentResolver couldn't open $uri outputStream")

                    val responseBody = downloadFileFromInternet(randomRemoteUrl)

                    if (responseBody == null) {
                        _isDownloading.postValue(false)
                        return@withContext
                    }

                    // .use is an extension function that closes the output stream where we're
                    // saving the file content once its lambda is finished being executed
                    responseBody.use {
                        outputStream.use {
                            responseBody.byteStream().copyTo(it)
                        }
                    }

                    Log.d(TAG, "File downloaded ($uri)")

                    val path = getMediaStoreEntryPathApi29(uri)
                        ?: throw Exception("ContentResolver couldn't find $uri")

                    // We scan the newly added file to make sure MediaStore.Downloads is always up
                    // to date
                    scanFilePath(path, responseBody.contentType().toString()) {
                        Log.d(TAG, "MediaStore updated ($path)")

                        viewModelScope.launch {
                            val fileDetails = getFileDetailsApi29(uri)
                            Log.d(TAG, "New file: $fileDetails")

                            _currentFileEntry.postValue(fileDetails)
                            _isDownloading.postValue(false)
                        }
                    }
                } else {
                    val file = addFileToDownloadsApi21(filename)
                    val outputStream = file.outputStream()

                    val responseBody = downloadFileFromInternet(randomRemoteUrl)

                    if (responseBody == null) {
                        _isDownloading.postValue(false)
                        return@withContext
                    }

                    // .use is an extension function that closes the output stream where we're
                    // saving the file content once its lambda is finished being executed
                    responseBody.use {
                        outputStream.use {
                            responseBody.byteStream().copyTo(it)
                        }
                    }

                    Log.d(TAG, "File downloaded (${file.absolutePath})")

                    // We scan the newly added file to make sure MediaStore.Files is always up to
                    // date
                    scanFilePath(file.path, responseBody.contentType().toString()) {
                        Log.d(TAG, "MediaStore updated ($file.path)")

                        viewModelScope.launch {
                            val fileDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                getFileDetailsApi26(file.absolutePath)
                            } else {
                                getFileDetailsApi21(file.absolutePath)
                            }
                            Log.d(TAG, "New file: $fileDetails")

                            _currentFileEntry.postValue(fileDetails)
                            _isDownloading.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                _isDownloading.postValue(false)
            }
        }
    }

    /**
     * Downloads a random file from internet and saves its content to the specified outputStream
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun downloadFileFromInternet(url: String): ResponseBody? {
        // We use OkHttp to create HTTP request
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(request).execute()
            return@withContext response.body
        }
    }

    /**
     * Create a file inside the Download folder using java.io API
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun addFileToDownloadsApi21(filename: String): File {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)

        // Get path of the destination where the file will be saved
        val newNonMediaFile = File(downloadsFolder, filename)

        return withContext(Dispatchers.IO) {
            // Create new file if it does not exist, throw exception otherwise
            if (!newNonMediaFile.createNewFile()) {
                throw Exception("File ${newNonMediaFile.name} already exists")
            }

            return@withContext newNonMediaFile
        }
    }

    /**
     * Create a file inside the Download folder using MediaStore API
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun addFileToDownloadsApi29(filename: String): Uri {
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        return withContext(Dispatchers.IO) {
            val newFile = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
            }

            // This method will perform a binder transaction which is better to execute off the main
            // thread
            return@withContext context.contentResolver.insert(collection, newFile)
                ?: throw Exception("MediaStore Uri couldn't be created")
        }
    }

    /**
     * When adding a file (using java.io or ContentResolver APIs), MediaStore might not be aware of
     * the new entry or doesn't have an updated version of it. That's why some entries have 0 bytes
     * size, even though the file is definitely not empty. MediaStore will eventually scan the file
     * but it's better to do it ourselves to have a fresher state whenever we can
     */
    private suspend fun scanFilePath(path: String, mimeType: String, callback: () -> Unit) {
        withContext(Dispatchers.IO) {
            MediaScannerConnection.scanFile(context, arrayOf(path), arrayOf(mimeType)) { _, _ ->
                callback()
            }
        }
    }

    /**
     * Get a path for a MediaStore entry as it's needed when calling MediaScanner
     */
    private suspend fun getMediaStoreEntryPathApi29(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(FileColumns.DATA),
                null,
                null,
                null
            ) ?: return@withContext null

            cursor.use {
                if (!cursor.moveToFirst()) {
                    return@withContext null
                }

                return@withContext cursor.getString(cursor.getColumnIndexOrThrow(FileColumns.DATA))
            }
        }
    }

    /**
     * Get file details on Api 21
     *
     * It uses the classic java.io APIs but can't get the added time as there's not a reliable way
     * to do so until Api 26
     */
    private suspend fun getFileDetailsApi21(path: String): FileEntry? {
        return withContext(Dispatchers.IO) {
            val file = File(path)

            if (!file.exists()) {
                return@withContext null
            }

            return@withContext FileEntry(
                filename = file.name,
                size = file.length(),
                mimeType = URLConnection.guessContentTypeFromName(file.name),
                // There are no reliable ways to get the added time on Api 26
                addedAt = -1,
                path = path
            )
        }
    }

    /**
     * Get file details on Api 26
     *
     * It uses java.nio APIs to get the mime type and added time properties
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getFileDetailsApi26(path: String): FileEntry? {
        return withContext(Dispatchers.IO) {
            val file = File(path)

            if (!file.exists()) {
                return@withContext null
            }

            return@withContext FileEntry(
                filename = file.name,
                size = file.length(),
                mimeType = Files.probeContentType(file.toPath()),
                addedAt = (
                    Files.getAttribute(
                        file.toPath(),
                        "creationTime"
                    ) as FileTime
                    ).toMillis(),
                path = path
            )
        }
    }

    /**
     * Get file details on Api 29
     *
     * It uses MediaStore to all the file properties
     */
    private suspend fun getFileDetailsApi29(uri: Uri): FileEntry? {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    FileColumns.DISPLAY_NAME,
                    FileColumns.SIZE,
                    FileColumns.MIME_TYPE,
                    FileColumns.DATE_ADDED,
                    FileColumns.DATA
                ),
                null,
                null,
                null
            ) ?: return@withContext null

            cursor.use {
                if (!cursor.moveToFirst()) {
                    return@withContext null
                }

                val displayNameColumn = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(FileColumns.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(FileColumns.DATE_ADDED)
                val dataColumn = cursor.getColumnIndexOrThrow(FileColumns.DATA)

                return@withContext FileEntry(
                    filename = cursor.getString(displayNameColumn),
                    size = cursor.getLong(sizeColumn),
                    mimeType = cursor.getString(mimeTypeColumn),
                    addedAt = cursor.getLong(dateAddedColumn),
                    path = cursor.getString(dataColumn),
                )
            }
        }
    }
}

data class FileEntry(
    val filename: String,
    val size: Long,
    val mimeType: String,
    val addedAt: Long,
    val path: String
) {
    companion object {
        /**
         * Create a [FileEntry] from a [Bundle] when loading [SavedStateHandle]
         */
        fun fromBundle(bundle: Bundle): FileEntry? {
            return if (bundle.containsKey("filename") &&
                bundle.containsKey("size") &&
                bundle.containsKey("mimeType") &&
                bundle.containsKey("addedAt") &&
                bundle.containsKey("path")
            ) {
                FileEntry(
                    filename = bundle.getString("filename")!!,
                    size = bundle.getLong("size"),
                    mimeType = bundle.getString("mimeType")!!,
                    addedAt = bundle.getLong("addedAt"),
                    path = bundle.getString("path")!!
                )
            } else {
                null
            }
        }
    }

    /**
     * Export [FileEntry] as a [Bundle] when saving [SavedStateHandle]
     */
    fun toBundle() = bundleOf(
        "filename" to filename,
        "size" to size,
        "mimeType" to mimeType,
        "addedAt" to addedAt,
        "path" to path
    )
}
