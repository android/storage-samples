package com.samples.storage.scopedstorage.mediastore

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.ImageUtils
import com.samples.storage.scopedstorage.common.MediaStoreUtils
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.util.Log
import com.samples.storage.scopedstorage.common.FileResource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.File
import java.io.IOException


class AddMediaFileViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val TAG = this.javaClass.simpleName

    private val context: Context
        get() = getApplication()

    val canWriteInMediaStore: Boolean
        get() = MediaStoreUtils.canWriteInMediaStore(context)

    private val _errorFlow =  MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    /**
     * We keep the current media [Uri] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    val addedMedia: LiveData<FileResource?> = savedStateHandle.getLiveData<FileResource?>("addedMedia")

    fun addImage() {
        viewModelScope.launch {
            val filename = "generated-${System.currentTimeMillis()}.jpg"
            val randomColor = ImageUtils.getRandomColor()
            val generatedBitmap = ImageUtils.generateImage(randomColor, 500, 500)

            val imageUri = MediaStoreUtils.createImageUri(context, filename)

            if (imageUri == null) {
                _errorFlow.emit("Couldn't create an image Uri\n$filename")
            } else {
                try {
                    context.contentResolver.openOutputStream(imageUri, "w")?.use { outputStream ->
                        generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    savedStateHandle["addedMedia"] = MediaStoreUtils.getResourceByUri(context, imageUri)
                } catch (e: IOException) {
                    Log.e(TAG, e.printStackTrace().toString())
                    _errorFlow.emit("Couldn't save the image\n$imageUri")
                }
            }
        }
    }
}