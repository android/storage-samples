package com.samples.storage.scopedstorage.mediastore

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.MediaStoreUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListMediaFilesViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = this::class.java.simpleName
    }

    private val context: Context
        get() = getApplication()

    val canReadInMediaStore: Boolean
        get() = MediaStoreUtils.canReadInMediaStore(context)

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    class MediaQuery(
        val loading: Boolean,
        val success: Boolean,
        val results: List<FileResource> = emptyList()
    )

    private val _mediaQuery = MutableStateFlow(MediaQuery(loading = true, success = false))
    val mediaQuery = _mediaQuery.asStateFlow()

    fun loadMedia() {
        viewModelScope.launch {
            _mediaQuery.value = MediaQuery(loading = true, success = false)

            // Simulate delay
            delay(3000L)

            try {
                val results = MediaStoreUtils.getMediaResources(context, limit = 10)
                _mediaQuery.value = MediaQuery(loading = false, success = true, results = results)
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
                _mediaQuery.value = MediaQuery(loading = false, success = false)
                _errorFlow.emit("Couldn't query MediaStore")
            }
        }
    }
}