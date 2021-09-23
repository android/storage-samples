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