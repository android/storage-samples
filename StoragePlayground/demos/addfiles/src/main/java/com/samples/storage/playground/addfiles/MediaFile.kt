/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.samples.storage.playground.addfiles

import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

enum class MediaFile {
    Image {
        override val extension = "jpg"
        override val mimeType = "image/jpeg"
        override val list = listOf(
            "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Instant%20Upload/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Google%20Drive/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20-%202013%20in%20Searches/card.jpg",
            "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Explore%20Treasure%20Mode%20with%20Google%20Maps/card.jpg"
        )

        override fun getSharedFolder(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        override fun getMediaStoreCollection(): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        }
    },

    Video {
        override val extension = "mp4"
        override val mimeType = "video/mp4"
        override val list = listOf(
            "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Instant%20Upload.mp4",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Google%20Drive.mp4",
            "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20-%202013%20in%20Searches.mp4",
            "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Explore%20Treasure%20Mode%20with%20Google%20Maps.mp4"
        )

        override fun getSharedFolder(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

        override fun getMediaStoreCollection(): Uri {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        }
    };

    abstract val extension: String
    abstract val mimeType: String
    abstract val list: List<String>

    fun random() = list.random()
    abstract fun getSharedFolder(): File
    abstract fun getMediaStoreCollection(): Uri
}