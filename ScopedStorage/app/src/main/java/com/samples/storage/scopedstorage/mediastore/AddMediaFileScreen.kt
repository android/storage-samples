package com.samples.storage.scopedstorage.mediastore

import android.text.format.Formatter.formatShortFileSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.samples.storage.scopedstorage.Demos
import com.samples.storage.scopedstorage.HomeRoute
import com.samples.storage.scopedstorage.R
import com.samples.storage.scopedstorage.common.FileResource
import com.samples.storage.scopedstorage.common.compositeBorderColor
import com.skydoves.landscapist.glide.GlideImage

@ExperimentalFoundationApi
@Composable
fun AddMediaFileScreen(
    navController: NavController,
    viewModel: AddMediaFileViewModel = viewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val error by viewModel.errorFlow.collectAsState(null)
    val addedMedia by viewModel.addedMedia.observeAsState()

    LaunchedEffect(error) {
        println("Hi LaunchedEffect")
        error?.let { scaffoldState.snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Demos.AddMediaFile.name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack(HomeRoute, false) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go to Previous screen")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                if (addedMedia != null) {
                    MediaFilePreviewCard(addedMedia!!)
                } else {
                    EmptyFilePreviewCard()
                }

                LazyVerticalGrid(cells = GridCells.Fixed(2)) {
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = { viewModel.addImage() }) {
                            Text(stringResource(R.string.demo_add_image_label))
                        }
                    }
                    item {
                        Button(
                            modifier = Modifier.padding(16.dp),
                            onClick = { viewModel.addVideo() }) {
                            Text(stringResource(R.string.demo_add_video_label))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EmptyFilePreviewCard() {
    Card(
        elevation = 0.dp,
        border = BorderStroke(width = 1.dp, color = compositeBorderColor()),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.empty_state_label),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
fun MediaFilePreviewCard(resource: FileResource) {
    val context = LocalContext.current
    val fileMetadata = "${resource.mimeType} - ${formatShortFileSize(context, resource.size)}"

    Card(
        elevation = 0.dp,
        border = BorderStroke(width = 1.dp, color = compositeBorderColor()),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column {
            GlideImage(
                imageModel = resource.uri,
                contentDescription = null
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = resource.filename, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = fileMetadata, style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = resource.path, style = MaterialTheme.typography.caption)
            }
        }
    }
}