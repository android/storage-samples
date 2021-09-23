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

import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore.Files.FileColumns
import kotlinx.parcelize.Parcelize

/**
 * Represents a File entry.
 *
 * @property uri Entry uri.
 * @property filename File name with extension.
 * @property size Size of the file in bytes.
 * @property type Entry file type.
 * @property mimeType Mime type of the file.
 */
@Parcelize
data class FileResource(
    val uri: Uri,
    val filename: String,
    val size: Long,
    val type: FileType,
    val mimeType: String,
    val path: String?,
) : Parcelable

/**
 *  Media type enum class representing the [FileColumns.MEDIA_TYPE] column
 */
enum class FileType(val value: Int) {
    /**
     * Representing [FileColumns.MEDIA_TYPE_NONE]
     */
    NONE(0),

    /**
     * Representing [FileColumns.MEDIA_TYPE_IMAGE]
     */
    IMAGE(1),

    /**
     * Representing [FileColumns.MEDIA_TYPE_AUDIO]
     */
    AUDIO(2),

    /**
     * Representing [FileColumns.MEDIA_TYPE_VIDEO]
     */
    VIDEO(3),

    /**
     * Representing [FileColumns.MEDIA_TYPE_PLAYLIST]
     */
    PLAYLIST(4),

    /**
     * Representing [FileColumns.MEDIA_TYPE_SUBTITLE]
     */
    SUBTITLE(5),

    /**
     * Representing [FileColumns.MEDIA_TYPE_DOCUMENT]
     */
    DOCUMENT(6);

    companion object {
        /**
         * Returns the matching [FileType] enum given an int value
         *
         * @param value int value of the [FileType] as written in [FileColumns.MEDIA_TYPE] column
         */
        fun getEnum(value: Int) = values().find {
            it.value == value
        } ?: throw IllegalArgumentException("Unknown MediaStoreType value")
    }
}