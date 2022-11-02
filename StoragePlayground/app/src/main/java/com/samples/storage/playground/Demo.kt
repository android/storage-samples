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

package com.samples.storage.playground

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

data class Demo(val route: String, val label: String, val icon: ImageVector)

object Demos {
    val PhotoPicker = Demo("photo-picker", "Photo Picker", Icons.Filled.PhotoLibrary)

    val AddMediaFile = Demo("add-media-file", "Add Media File", Icons.Filled.AddPhotoAlternate)
    val AddFileToDownloads =
        Demo("add-files-to-downloads", "Add Files To Downloads", Icons.Filled.AddCircle)

    val CreateDocumentFile =
        Demo("create-document-file", "Create Document File", Icons.Filled.NoteAdd)
    val ReadDocumentFile = Demo("read-document-file", "Read Document File", Icons.Filled.AttachFile)
    val EditDocumentFile = Demo("edit-document-file", "Edit Document File", Icons.Filled.Edit)
    val ListFolderFiles = Demo("list-folder-files", "List Folder Files", Icons.Filled.FolderOpen)

    val ListMediaFiles = Demo("list-media-files", "List Media Files", Icons.Filled.ImageSearch)

    val list = listOf(
        PhotoPicker,

        AddMediaFile,
        AddFileToDownloads,

        CreateDocumentFile,
        ReadDocumentFile,
        EditDocumentFile,
        ListFolderFiles,

        ListMediaFiles,
    )
}