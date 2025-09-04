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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.samples.storage.uielements.BackButton
import com.samples.storage.uielements.OptionListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMediaFileScreen(
    navController: NavController,
    viewModel: AddMediaFileViewModel = viewModel()
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Media File", fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    BackButton(navController)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::download,
                icon = {
                    if (state.status == AddMediaFileViewModel.Status.LOADING) {
                        CircularProgressIndicator()
                    } else {
                        Icon(Icons.Filled.Download, null)
                    }
                },
                text = { Text(text = "Download ${state.fileType}") },
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            OptionListItem(
                label = "File Type",
                icon = Icons.Filled.Tune,
                values = MediaFile.values(),
                currentValue = state.fileType,
                onChange = viewModel::onFileTypeChange
            )
            Divider()
            OptionListItem(
                label = "Storage Destination",
                icon = Icons.Filled.Folder,
                values = AddMediaFileViewModel.StorageDestination.values(),
                currentValue = state.storageDestination,
                onChange = viewModel::onDestinationChange
            )
            Divider()
        }
    }
}