/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.samples.storage.saf

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import kotlin.random.Random

/** Number of bytes to read at a time from an open stream */
private const val FILE_BUFFER_SIZE_BYTES = 1024

/**
 * ViewModel contains various examples for how to work with the contents of documents
 * opened with the Storage Access Framework.
 */
class SafFragmentViewModel : ViewModel() {

    /**
     * It's easiest to work with documents selected with the [Intent.ACTION_CREATE_DOCUMENT] action
     * by simply opening an [OutputStream]. In this example we're generating some random text
     * based on the words found in "Lorem Ipsum".
     */
    suspend fun createDocumentExample(outputStream: OutputStream): String {

        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(Dispatchers.IO) {
            val lines = mutableListOf<String>()

            for (lineNumber in 1..Random.nextInt(1, 5)) {
                val line = "hello world ".repeat(Random.nextInt(1, 5))
                lines += line.capitalize(Locale.US)
            }

            val contents = lines.joinToString(separator = System.lineSeparator())

            outputStream.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write(contents)
            }
            contents
        }
    }

    /**
     * Similar to [Intent.ACTION_CREATE_DOCUMENT], it's easiest to work with documents selected
     * with the [Intent.ACTION_OPEN_DOCUMENT] action by simply opening an [InputStream] or
     * [OutputStream], depending on the need. In this example, since we don't want to disturb the
     * contents of the file, we're just going to use an [InputStream] to generate a hash of
     * the file's contents.
     *
     * Since hashing the contents of a large file may take some time, this is done in a
     * suspend function with the [Dispatchers.IO] coroutine context.
     */
    suspend fun openDocumentExample(inputStream: InputStream): String {
        @Suppress("BlockingMethodInNonBlockingContext")
        return withContext(Dispatchers.IO) {
            inputStream.use { stream ->
                val messageDigest = MessageDigest.getInstance("SHA-256")

                val buffer = ByteArray(FILE_BUFFER_SIZE_BYTES)
                var bytesRead = stream.read(buffer)
                while (bytesRead > 0) {
                    messageDigest.update(buffer, 0, bytesRead)
                    bytesRead = stream.read(buffer)
                }
                val hashResult = messageDigest.digest()
                hashResult.joinToString(separator = ":") { "%02x".format(it) }
            }
        }
    }

    /**
     * Simple example of using [DocumentFile] to get all the documents in a folder (by using
     * [Intent.ACTION_OPEN_DOCUMENT_TREE]).
     * It's possible to use [DocumentsContract] and [ContentResolver] directly, but using
     * [DocumentFile] allows us to access an easier to use API.
     *
     * While it's _possible_ to search across multiple directories and recursively work with files
     * via SAF, there can be significant performance penalties to this type of usage. If your
     * use case requires this, consider looking into the permission [MANAGE_EXTERNAL_STORAGE].
     *
     * Accessing any field in the [DocumentFile] object, aside from [DocumentFile.getUri],
     * ultimately performs a lookup with the system's [ContentResolver], and should thus be
     * performed off the main thread, which is why we're doing this transformation from
     * [DocumentFile] to file name and [Uri] in a coroutine.
     */
    suspend fun listFiles(folder: DocumentFile): List<Pair<String, Uri>> {
        return withContext(Dispatchers.IO) {
            if (folder.isDirectory) {
                folder.listFiles().mapNotNull { file ->
                    if (file.name != null) Pair(file.name!!, file.uri) else null
                }
            } else {
                emptyList()
            }
        }
    }
}
