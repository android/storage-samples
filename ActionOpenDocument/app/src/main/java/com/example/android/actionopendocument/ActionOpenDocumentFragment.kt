/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.android.actionopendocument

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * This fragment has a big [ImageView] that shows PDF pages, and 2 [Button]s to move between pages.
 * We use a [PdfRenderer] to render PDF pages as [Bitmap]s.
 */
class ActionOpenDocumentFragment : androidx.fragment.app.Fragment(), View.OnClickListener {

    /**
     * The filename of the PDF.
     */
    private val FILENAME = "sample.pdf"

    /**
     * Key string for saving the state of current page index.
     */
    private val STATE_CURRENT_PAGE_INDEX = "current_page_index"

    /**
     * String for logging.
     */
    private val TAG = "ActionOpenDocumentFragment"

    /**
     * The initial page index of the PDF.
     */
    private val INITIAL_PAGE_INDEX = 0

    /**
     * File descriptor of the PDF.
     */
    private lateinit var fileDescriptor: ParcelFileDescriptor

    /**
     * [PdfRenderer] to render the PDF.
     */
    private lateinit var pdfRenderer: PdfRenderer

    /**
     * Page that is currently shown on the screen.
     */
    private lateinit var currentPage: PdfRenderer.Page

    /**
     * [ImageView] that shows a PDF page as a [Bitmap].
     */
    private lateinit var imageView: ImageView

    /**
     * [Button] to move to the previous page.
     */
    private lateinit var btnPrevious: Button

    /**
     * [Button] to move to the next page.
     */
    private lateinit var btnNext: Button

    /**
     * PDF page index.
     */
    private var pageIndex: Int = INITIAL_PAGE_INDEX

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_renderer_basic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.image)
        btnPrevious = view.findViewById<Button>(R.id.previous).also { it.setOnClickListener(this) }
        btnNext = view.findViewById<Button>(R.id.next).also { it.setOnClickListener(this)}

        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (savedInstanceState != null) {
            pageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, INITIAL_PAGE_INDEX)
        } else {
            pageIndex = INITIAL_PAGE_INDEX
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            openRenderer(activity)
            showPage(pageIndex)
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
    }

    override fun onStop() {
        try {
            closeRenderer()
        } catch (e: IOException) {
            Log.d(TAG, e.toString())
        }
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_CURRENT_PAGE_INDEX, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    /**
     * Sets up a [PdfRenderer] and related resources.
     */
    @Throws(IOException::class)
    private fun openRenderer(context: Context?) {
        if (context == null) return

        // In this sample, we read a PDF from the assets directory.
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            val asset = context.assets.open(FILENAME)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size = asset.read(buffer)
            while (size != -1) {
                output.write(buffer, 0, size)
                size = asset.read(buffer)
            }
            asset.close()
            output.close()
        }
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        // This is the PdfRenderer we use to render the PDF.
        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(pageIndex)
    }

    /**
     * Closes the [PdfRenderer] and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
        fileDescriptor.close()
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) return

        // Make sure to close the current page before opening another one.
        currentPage.close()
        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index)
        // Important: the destination bitmap must be ARGB (not RGB).
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        // We are ready to show the Bitmap to user.
        imageView.setImageBitmap(bitmap)
        updateUi()
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private fun updateUi() {
        val index = currentPage.index
        val pageCount = pdfRenderer.pageCount
        btnPrevious.isEnabled = (0 != index)
        btnNext.isEnabled = (index + 1 < pageCount)
        activity?.title = getString(R.string.app_name_with_index, index + 1, pageCount)
    }

    /**
     * Returns the page count of of the PDF.
     */
    fun getPageCount() = pdfRenderer.pageCount

    override fun onClick(view: View) {
        when (view.id) {
            R.id.previous -> {
                // Move to the previous page/
                showPage(currentPage.index - 1)
            }
            R.id.next -> {
                // Move to the next page.
                showPage(currentPage.index + 1)
            }
        }
    }

}
