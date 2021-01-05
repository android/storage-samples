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

package com.example.graygallery.ui

import android.app.Application
import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.graygallery.R
import com.example.graygallery.utils.Source
import com.example.graygallery.utils.copyImageFromStream
import com.example.graygallery.utils.generateFilename
import com.example.graygallery.utils.applyGrayscaleFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

private const val FILEPATH_XML_KEY = "files-path"
private const val RANDOM_IMAGE_URL = "https://source.unsplash.com/random/500x500"
val ACCEPTED_MIMETYPES = arrayOf("image/jpeg", "image/png")

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val httpClient by lazy { OkHttpClient() }

    private val context: Context
        get() = getApplication()

    private val _notification = MutableLiveData<String>()
    val notification: LiveData<String>
        get() = _notification

    private val imagesFolder: File by lazy { getImagesFolder(context) }

    private val _images = MutableLiveData(emptyList<File>())
    val images: LiveData<List<File>>
        get() = _images

    fun loadImages() {
        viewModelScope.launch {
            val images = withContext(Dispatchers.IO) {
                imagesFolder.listFiles().toList()
            }

            _images.postValue(images)
        }
    }

    fun saveImageFromCamera(bitmap: Bitmap) {
        val imageFile = File(imagesFolder, generateFilename(Source.CAMERA))
        val imageStream = FileOutputStream(imageFile)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val grayscaleBitmap = withContext(Dispatchers.Default) {
                        applyGrayscaleFilter(bitmap)
                    }
                    grayscaleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageStream)
                    imageStream.flush()
                    imageStream.close()

                    _notification.postValue("Camera image saved")

                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error writing bitmap", e)
                }
            }
        }
    }

    fun copyImageFromUri(uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.let {
                    // TODO: Apply grayscale filter before saving image
                    copyImageFromStream(it, imagesFolder)
                    _notification.postValue("Image copied")
                }
            }
        }
    }

    fun saveRandomImageFromInternet() {
        viewModelScope.launch {
            val request = Request.Builder().url(RANDOM_IMAGE_URL).build()

            withContext(Dispatchers.IO) {
                val response = httpClient.newCall(request).execute()

                response.body?.let { responseBody ->
                    val imageFile = File(imagesFolder, generateFilename(Source.INTERNET))
                    // TODO: Apply grayscale filter before saving image
                    imageFile.writeBytes(responseBody.bytes())
                    _notification.postValue("Image downloaded")
                }

                if (!response.isSuccessful) {
                    _notification.postValue("Failed to download image")
                }
            }
        }
    }

    fun clearFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                imagesFolder.deleteRecursively()
                _images.postValue(emptyList())
                _notification.postValue("Images cleared")
            }
        }
    }
}

private fun getImagesFolder(context: Context): File {
    val xml = context.resources.getXml(R.xml.filepaths)

    val attributes = getAttributesFromXmlNode(xml, FILEPATH_XML_KEY)

    val folderPath = attributes["path"]
        ?: error("You have to specify the sharable directory in res/xml/filepaths.xml")

    return File(context.filesDir, folderPath).also {
        if (!it.exists()) {
            it.mkdir()
        }
    }
}

// TODO: Make the function suspend
private fun getAttributesFromXmlNode(
    xml: XmlResourceParser,
    nodeName: String
): Map<String, String> {
    while (xml.eventType != XmlResourceParser.END_DOCUMENT) {
        if (xml.eventType == XmlResourceParser.START_TAG) {
            if (xml.name == nodeName) {
                if (xml.attributeCount == 0) {
                    return emptyMap()
                }

                val attributes = mutableMapOf<String, String>()

                for (index in 0 until xml.attributeCount) {
                    attributes[xml.getAttributeName(index)] = xml.getAttributeValue(index)
                }

                return attributes
            }
        }

        xml.next()
    }

    return emptyMap()
}
