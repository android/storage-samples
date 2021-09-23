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