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

package com.samples.storage.scopedstorage.mediastore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.samples.storage.scopedstorage.Demos
import com.samples.storage.scopedstorage.HomeRoute
import com.samples.storage.scopedstorage.R
import com.samples.storage.scopedstorage.common.DocumentFilePreviewCard
import com.samples.storage.scopedstorage.mediastore.AddFileToDownloadsViewModel.FileType

@ExperimentalFoundationApi
@Composable
fun AddFileToDownloadsScreen(
    navController: NavController,
    viewModel: AddFileToDownloadsViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val error by viewModel.errorFlow.collectAsState(null)
    val addedMedia by viewModel.addedFile.observeAsState()

    LaunchedEffect(error) {
        error?.let { scaffoldState.snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.AddFileToDownloads.name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack(HomeRoute, false) }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_label)
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                if (addedMedia != null) {
                    DocumentFilePreviewCard(addedMedia!!)
                } else {
                    IntroCard()
                }

                LazyVerticalGrid(cells = GridCells.Fixed(2)) {
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = { viewModel.addFile(FileType.Pdf) }) {
                            Text(stringResource(R.string.demo_add_pdf_label))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = { viewModel.addFile(FileType.Zip) }) {
                            Text(stringResource(R.string.demo_add_zip_label))
                        }
                    }
                }
            }
        }
    )
}
