package com.samples.storage.scopedstorage.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

/**
 * Number of bytes to read at a time from an open stream
 */
private const val FILE_BUFFER_SIZE_BYTES = 1024

object FileUtils {
    suspend fun getInputStreamChecksum(inputStream: InputStream): String {
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
                return@withContext hashResult.joinToString("") { "%02x".format(it) }
            }
        }
    }
}