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
import androidx.lifecycle.MutableLiveData
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

    // [TakePicture] and [TakeVideo] activityResult actions isn't returning the URI once it's
    // returning the result, so we need to keep the temporarily created URI until the action is
    // finished
    private val _temporaryMediaUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val temporaryMediaUri: StateFlow<Uri?> = _temporaryMediaUri

    // We create a URI where the camera will store the image
    fun createPhotoUri(filename: String): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val newImage = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)

            // On Android 10 (API 29), we can add an entry to MediaStore without making it visible
            // to other apps until we complete writing its content by using the IS_PENDING flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(imageCollection, newImage)
        _temporaryMediaUri.value =uri

        return uri
    }

    // We create a URI where the camera will store the video
    fun createVideoUri(filename: String): Uri? {
        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val newVideo = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)

            // On Android 10 (API 29), we can add an entry to MediaStore without making it visible
            // to other apps until we complete writing its content by using the IS_PENDING flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
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

data class Media(val filename: String, val size: Long, val uri: Uri)