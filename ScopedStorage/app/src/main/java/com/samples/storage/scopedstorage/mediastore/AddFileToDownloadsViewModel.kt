package com.samples.storage.scopedstorage.mediastore

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.MediaStoreUtils
import com.samples.storage.scopedstorage.common.MediaStoreUtils.scanPath
import com.samples.storage.scopedstorage.common.MediaStoreUtils.scanUri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AddFileToDownloadsViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        private val TAG = this::class.java.simpleName
        const val ADDED_FILE_KEY = "addedFile"
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
    val addedFile: LiveData<FileResource?> =
        savedStateHandle.getLiveData<FileResource?>(ADDED_FILE_KEY)

    sealed class FileType(val extension: String, val mimeType: String) {
        object Pdf : FileType("pdf", "application/pdf")
        object Zip : FileType("zip", "application/zip")
    }

    private fun generateFilename(extension: String) =
        "added-${System.currentTimeMillis()}.$extension"

    fun addFile(type: FileType) {
        viewModelScope.launch {
            var filename = generateFilename(type.extension)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = MediaStoreUtils.createDownloadUri(context, filename)
                    ?: return@launch _errorFlow.emit("Couldn't create a ${type.extension} Uri\n$filename")

                try {
                    context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                        context.assets.open("sample.${type.extension}").use { inputStream ->
                            inputStream.copyTo(outputStream)
                            scanUri(context, uri, type.mimeType)
                            savedStateHandle[ADDED_FILE_KEY] =
                                MediaStoreUtils.getResourceByUri(context, uri)
                        }
                    }

                } catch (e: IOException) {
                    Log.e(TAG, e.printStackTrace().toString())
                    _errorFlow.emit("Couldn't save the ${type.extension}\n$uri")
                }
            } else {
                val downloadsFolder =
                    Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)

                // We re-generate the filename until we can confirm its uniqueness
                while (File(downloadsFolder, filename).exists()) {
                    filename = generateFilename(type.extension)
                }

                val file = File(downloadsFolder, filename)

                try {
                    file.outputStream().use { outputStream ->
                        context.assets.open("sample.${type.extension}").use { inputStream ->
                            inputStream.copyTo(outputStream)
                            scanPath(context, file.absolutePath, type.mimeType)?.let { uri ->
                                savedStateHandle[ADDED_FILE_KEY] =
                                    MediaStoreUtils.getResourceByUri(context, uri)
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.printStackTrace().toString())
                    _errorFlow.emit("Couldn't save the ${type.extension}\n${file.absolutePath}")
                }
            }
        }
    }
}