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