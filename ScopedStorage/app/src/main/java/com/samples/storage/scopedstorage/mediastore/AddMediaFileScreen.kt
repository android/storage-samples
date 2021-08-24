package com.samples.storage.scopedstorage.mediastore

import android.text.format.Formatter.formatShortFileSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.samples.storage.scopedstorage.Demos
import com.samples.storage.scopedstorage.R
import com.samples.storage.scopedstorage.common.compositeBorderColor

@ExperimentalFoundationApi
@Composable
fun AddMediaFileScreen(viewModel: AddMediaFileViewModel = viewModel()) {
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
                title = { Text(stringResource(Demos.AddMediaFile.name)) }
            )
        },
        content = { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                if(addedMedia != null) {
                    MediaFilePreviewCard(
                        filename = addedMedia!!.filename,
                        mimeType = addedMedia!!.mimeType,
                        size = addedMedia!!.size,
                        path = addedMedia!!.path
                    )
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
                            onClick = { /*TODO*/ }) {
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
fun MediaFilePreviewCard(filename: String, mimeType: String, size: Long, path: String) {
    val fileMetadata = "$mimeType - ${formatShortFileSize(null, size)}"

    Card(
        elevation = 0.dp,
        border = BorderStroke(width = 1.dp, color = compositeBorderColor()),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column {
            Image(
                painter = rememberImagePainter("https://lh3.googleusercontent.com/JT8ts9Khjsle_n-AWenqQkqHKtImeYr7q0DKAvvRy5KHl2edYmB_oUMTq70dVse_cslq0joryDbr4KLu1Xabo5Lal5908IZcftWotV8WTp4IECLitIU=w1064-v0"),
                contentDescription = null,
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = filename, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = fileMetadata, style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = path, style = MaterialTheme.typography.caption)
            }
        }
    }
}