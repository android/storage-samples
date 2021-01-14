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
import java.security.MessageDigest

/** Number of bytes to read at a time from an open stream */
private const val FILE_BUFFER_SIZE_BYTES = 1024

/**
 * ViewModel contains various examples for how to work with the contents of documents
 * opened with the Storage Access Framework.
 */
class SafFragmentViewModel : ViewModel() {
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

    /**
     * It's easiest to work with documents selected with the [Intent.ACTION_OPEN_DOCUMENT] action
     * by simply opening an [InputStream] or [OutputStream], depending on the need. In this
     * example, since we don't want to disturb the contents of the file, we're just going to use
     * an [InputStream] to generate a hash of the file's contents.
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
                while (stream.read(buffer) > 0) {
                    messageDigest.update(buffer)
                }
                val hashResult = messageDigest.digest()
                hashResult.joinToString(separator = ":") { "%02x".format(it) }
            }
        }
    }
}