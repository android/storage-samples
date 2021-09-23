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

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.samples.storage.scopedstorage.Demos
import com.samples.storage.scopedstorage.HomeRoute
import com.samples.storage.scopedstorage.R
import com.samples.storage.scopedstorage.common.MediaFilePreviewCard
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@Composable
fun CaptureMediaFileScreen(
    navController: NavController,
    viewModel: CaptureMediaFileViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val error by viewModel.errorFlow.collectAsState(null)
    val capturedMedia by viewModel.capturedMedia.observeAsState()

    val scope = rememberCoroutineScope()
    var targetImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var targetVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
        targetImageUri?.let {
            viewModel.onImageCapture(it)
            targetImageUri = null
        }
    }
    val takeVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) {
        targetVideoUri?.let {
            viewModel.onVideoCapture(it)
            targetVideoUri = null
        }
    }


    LaunchedEffect(error) {
        error?.let { scaffoldState.snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.CaptureMediaFile.name)) },
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
                if (capturedMedia != null) {
                    MediaFilePreviewCard(capturedMedia!!)
                } else {
                    IntroCard()
                }

                LazyVerticalGrid(cells = GridCells.Fixed(2)) {
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = {
                                scope.launch {
                                    viewModel.createImageUri()?.let {
                                        targetImageUri = it
                                        takePicture.launch(it)
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.demo_capture_image_label))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = {
                                scope.launch {
                                    viewModel.createVideoUri()?.let {
                                        targetVideoUri = it
                                        takeVideo.launch(it)
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.demo_capture_video_label))
                        }
                    }
                }
            }
        }
    )
}