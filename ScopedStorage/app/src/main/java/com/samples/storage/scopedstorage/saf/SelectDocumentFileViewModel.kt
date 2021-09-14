package com.samples.storage.scopedstorage.saf

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.SafUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class SelectDocumentFileViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        private val TAG = this::class.java.simpleName
        const val SELECTED_FILE_KEY = "selectedFile"
    }

    private val context: Context
        get() = getApplication()

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    /**
     * We keep the current media [Uri] in the savedStateHandle to re-render it if there is a
     * configuration change and we expose it as a [LiveData] to the UI
     */
    val selectedFile: LiveData<FileResource?> =
        savedStateHandle.getLiveData<FileResource?>(SELECTED_FILE_KEY)

    @SuppressLint("NewApi")
    fun onFileSelect(uri: Uri) {
        viewModelScope.launch {
            savedStateHandle[SELECTED_FILE_KEY] = SafUtils.getResourceByUri(context, uri)

            try {
                savedStateHandle[SELECTED_FILE_KEY] = SafUtils.getResourceByUri(context, uri)
            } catch (e: Exception) {
                Log.e(TAG, e.printStackTrace().toString())
                _errorFlow.emit("Couldn't load $uri")
            }
        }
    }
}