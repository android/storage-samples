package com.samples.storage.scopedstorage.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SafUtils {
    /**
     * Returns a [FileResource] if it finds its related DocumentsProvider
     */
    suspend fun getResourceByUri(context: Context, uri: Uri): FileResource {
        return withContext(Dispatchers.IO) {

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )

            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            ) ?: throw Exception("Uri $uri could not be found")

            cursor.use {
                if (!cursor.moveToFirst()) {
                    throw Exception("Uri $uri could not be found")
                }

                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val sizeColumn =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val mimeTypeColumn =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

                FileResource(
                    uri = uri,
                    filename = cursor.getString(displayNameColumn),
                    size = cursor.getLong(sizeColumn),
                    type = FileType.DOCUMENT,
                    mimeType = cursor.getString(mimeTypeColumn),
                    path = null,
                )
            }
        }
    }

    suspend fun getThumbnail(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            return@withContext DocumentsContract.getDocumentThumbnail(
                context.contentResolver,
                uri,
                Point(512, 512),
                null
            )
        }
    }
}