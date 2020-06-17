/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.graygallery.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

enum class Source {
    CAMERA, FILEPICKER, INTERNET;

    override fun toString(): String {
        return name.toLowerCase()
    }
}

fun generateFilename(source: Source) = "$source-${System.currentTimeMillis()}.jpg"

suspend fun copyImageFromStream(input: InputStream, directory: File) {
    withContext(Dispatchers.IO) {
        input.copyTo(File(directory, generateFilename(Source.FILEPICKER)).outputStream())
    }
}