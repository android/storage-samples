/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.samples.storage.R
import com.samples.storage.databinding.FragmentSafBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_FILE_NAME = "SAF Demo File.txt"

/**
 * Fragment that demonstrates the most common ways to work with documents via the
 * Storage Access Framework (SAF).
 */
class SafFragment : Fragment() {
    private var _binding: FragmentSafBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SafFragmentViewModel by viewModels()

    private val actionCreateDocument = registerForActivityResult(CreateDocument()) { uri ->
        // If the user returns to this fragment without creating a file, uri will be null
        // In this case, we return void
        val documentUri = uri ?: return@registerForActivityResult

        // If we can't instantiate a `DocumentFile`, it probably means the file has been removed
        // or became unavailable (if the SD card has been removed).
        // In this case, we return void
        val documentFile = DocumentFile.fromSingleUri(requireContext(), documentUri)
            ?: return@registerForActivityResult

        // We launch a coroutine within the lifecycle of the viewmodel. The coroutine will be
        // automatically cancelled if the viewmodel is cleared
        viewLifecycleOwner.lifecycleScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            val documentStream = withContext(Dispatchers.IO) {
                requireContext().contentResolver.openOutputStream(documentUri)
            } ?: return@launch

            val text = viewModel.createDocumentExample(documentStream)
            binding.output.text =
                getString(R.string.saf_create_file_output, documentFile.name, text)
        }

        Log.d("SafFragment", "Created: ${documentFile.name}, type ${documentFile.type}")
    }

    private val actionOpenDocument = registerForActivityResult(OpenDocument()) { uri ->
        // If the user returns to this fragment without selecting a file, uri will be null
        // In this case, we return void
        val documentUri = uri ?: return@registerForActivityResult

        // If we can't instantiate a `DocumentFile`, it probably means the file has been removed
        // or became unavailable (if the SD card has been removed).
        // In this case, we return void
        val documentFile = DocumentFile.fromSingleUri(requireContext(), documentUri)
            ?: return@registerForActivityResult

        viewLifecycleOwner.lifecycleScope.launch {
            @Suppress("BlockingMethodInNonBlockingContext")
            val documentStream = withContext(Dispatchers.IO) {
                requireContext().contentResolver.openInputStream(documentUri)
            } ?: return@launch

            val text = viewModel.openDocumentExample(documentStream)
            binding.output.text = getString(R.string.saf_open_file_output, documentFile.name, text)
        }
    }

    private val actionOpenDocumentTree = registerForActivityResult(OpenDocumentTree()) { uri ->
        val documentUri = uri ?: return@registerForActivityResult
        val context = requireContext().applicationContext
        val parentFolder = DocumentFile.fromTreeUri(context, documentUri)
            ?: return@registerForActivityResult

        viewLifecycleOwner.lifecycleScope.launch {
            val text = viewModel.listFiles(parentFolder)
                .sortedBy { it.first }
                .joinToString { it.first }
            binding.output.text = getString(R.string.saf_folder_output, text)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafBinding.inflate(inflater, container, false)

        binding.createFile.setOnClickListener {
            // We ask the user to create a file with a preferred default filename, which can be
            // overwritten by the user
            actionCreateDocument.launch(DEFAULT_FILE_NAME)
        }

        binding.openFile.setOnClickListener {
            // We ask the user to select any file. If we want to select a specific one, we would do
            // this: `actionOpenDocument.launch(arrayOf("image/png", "image/gif"))`
            actionOpenDocument.launch(arrayOf("*/*"))
        }

        binding.openFolder.setOnClickListener {
            // We ask the user to select a folder. We can specify a preferred folder to be opened
            // if we have its URI and the device is running on API 26+
            actionOpenDocumentTree.launch(null)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
