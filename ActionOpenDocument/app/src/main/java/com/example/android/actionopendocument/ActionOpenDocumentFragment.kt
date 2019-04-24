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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import java.io.FileDescriptor
import java.io.IOException

/**
 * This fragment has a big [ImageView] that shows PDF pages, and 2 [Button]s to move between pages.
 * We use a [PdfRenderer] to render PDF pages as [Bitmap]s.
 */
class ActionOpenDocumentFragment : Fragment() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private var currentPageNumber: Int = INITIAL_PAGE_INDEX

    private lateinit var pdfPageView: ImageView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    val pageCount get() = pdfRenderer.pageCount

    companion object {
        private const val DOCUMENT_URI_ARGUMENT =
            "com.example.android.actionopendocument.args.DOCUMENT_URI_ARGUMENT"

        fun newInstance(documentUri: Uri): ActionOpenDocumentFragment {

            return ActionOpenDocumentFragment().apply {
                arguments = Bundle().apply {
                    putString(DOCUMENT_URI_ARGUMENT, documentUri.toString())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_renderer_basic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfPageView = view.findViewById(R.id.image)
        previousButton = view.findViewById<Button>(R.id.previous).apply {
            setOnClickListener {
                showPage(currentPage.index - 1)
            }
        }
        nextButton = view.findViewById<Button>(R.id.next).apply {
            setOnClickListener {
                showPage(currentPage.index + 1)
            }
        }

        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        currentPageNumber = savedInstanceState?.getInt(CURRENT_PAGE_INDEX_KEY, INITIAL_PAGE_INDEX)
            ?: INITIAL_PAGE_INDEX
    }

    override fun onStart() {
        super.onStart()

        val documentUri = arguments?.getString(DOCUMENT_URI_ARGUMENT)?.toUri() ?: return
        try {
            openRenderer(activity, documentUri)
            showPage(currentPageNumber)
        } catch (ioException: IOException) {
            Log.d(TAG, "Exception opening document", ioException)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ioException: IOException) {
            Log.d(TAG, "Exception closing document", ioException)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE_INDEX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    /**
     * Sets up a [PdfRenderer] and related resources.
     */
    @Throws(IOException::class)
    private fun openRenderer(context: Context?, documentUri: Uri) {
        if (context == null) return

        /**
         * It may be tempting to use `use` here, but [PdfRenderer] expects to take ownership
         * of the [FileDescriptor], and, if we did use `use`, it would be auto-closed at the
         * end of the block, preventing us from rendering additional pages.
         */
        val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r") ?: return

        // This is the PdfRenderer we use to render the PDF.
        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(currentPageNumber)
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
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * The way [PdfRenderer] works is that it allows for "opening" a page with the method
     * [PdfRenderer.openPage], which takes a (0 based) page number to open. This returns
     * a [PdfRenderer.Page] object, which represents the content of this page.
     *
     * There are two ways to render the content of a [PdfRenderer.Page].
     * [PdfRenderer.Page.RENDER_MODE_FOR_PRINT] and [PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY].
     * Since we're displaying the data on the screen of the device, we'll use the later.
     *
     * @param index The page index.
     */
    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) return

        currentPage.close()
        currentPage = pdfRenderer.openPage(index)

        // Important: the destination bitmap must be ARGB (not RGB).
        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)

        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPageView.setImageBitmap(bitmap)

        val pageCount = pdfRenderer.pageCount
        previousButton.isEnabled = (0 != index)
        nextButton.isEnabled = (index + 1 < pageCount)
        activity?.title = getString(R.string.app_name_with_index, index + 1, pageCount)
    }
}

/**
 * Key string for saving the state of current page index.
 */
private const val CURRENT_PAGE_INDEX_KEY =
    "com.example.android.actionopendocument.state.CURRENT_PAGE_INDEX_KEY"

private const val TAG = "ActionOpenDocumentFragment"
private const val INITIAL_PAGE_INDEX = 0

