package com.samples.storage.scopedstorage.common

import android.graphics.Bitmap
import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun MediaFilePreviewCard(resource: FileResource) {
    val context = LocalContext.current
    val formattedFileSize = Formatter.formatShortFileSize(context, resource.size)
    val fileMetadata = "${resource.mimeType} - $formattedFileSize"

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
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = resource.filename, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = fileMetadata, style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(12.dp))
                resource.path?.let { Text(text = it, style = MaterialTheme.typography.caption) }
            }
        }
    }
}

@Composable
fun DocumentFilePreviewCard(resource: FileResource) {
    val context = LocalContext.current
    val formattedFileSize = Formatter.formatShortFileSize(context, resource.size)
    val fileMetadata = "${resource.mimeType} - $formattedFileSize"

    val thumbnail by produceState<Bitmap?>(null, resource.uri) {
        value = SafUtils.getThumbnail(context, resource.uri)
    }


    Card(
        elevation = 0.dp,
        border = BorderStroke(width = 1.dp, color = compositeBorderColor()),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Column {
            thumbnail?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = resource.filename, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = fileMetadata, style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(12.dp))
                resource.path?.let { Text(text = it, style = MaterialTheme.typography.caption) }
            }
        }
    }
}