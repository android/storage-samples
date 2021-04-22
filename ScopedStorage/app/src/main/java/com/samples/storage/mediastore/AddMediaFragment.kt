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
package com.samples.storage.mediastore

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.samples.storage.databinding.FragmentAddMediaBinding
import kotlinx.coroutines.launch

class AddMediaFragment : Fragment() {
    private var _binding: FragmentAddMediaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddMediaViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddMediaBinding.inflate(inflater, container, false)

        // Every time currentMediaUri is changed, we update the ImageView
        viewModel.currentMediaUri.observe(viewLifecycleOwner) { uri ->
            Glide.with(this).load(uri).into(binding.mediaThumbnail)
        }

        binding.requestPermissionButton.setOnClickListener {
            actionRequestPermission.launch(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
        }

        binding.takePictureButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {

                if(viewModel.canWriteInMediaStore) {
                    viewModel.createPhotoUri(Source.CAMERA)?.let { uri ->
                        viewModel.saveTemporarilyPhotoUri(uri)
                        actionTakePicture.launch(uri)
                    }
                } else {
                    showPermissionSection()
                }
            }
        }

        binding.takeVideoButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {


                if(viewModel.canWriteInMediaStore) {
                    viewModel.createVideoUri(Source.CAMERA)?.let { uri ->
                        actionTakeVideo.launch(uri)
                    }
                } else {
                    showPermissionSection()
                }
            }
        }

        binding.downloadImageFromInternetButton.setOnClickListener {

            if(viewModel.canWriteInMediaStore) {
                binding.downloadImageFromInternetButton.isEnabled = false
                viewModel.saveRandomImageFromInternet {
                    // We re-enable the button once the download is done
                    // Keep in mind the logic is basic as it doesn't handle errors
                    binding.downloadImageFromInternetButton.isEnabled = true
                }
            } else {
                showPermissionSection()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        handlePermissionSectionVisibility()
    }

    private fun handlePermissionSectionVisibility() {
        if (viewModel.canWriteInMediaStore) {
            hidePermissionSection()
        } else {
            showPermissionSection()
        }
    }

    private fun hidePermissionSection() {
        binding.permissionSection.visibility = View.GONE
        binding.actions.visibility = View.VISIBLE
    }

    private fun showPermissionSection() {
        binding.permissionSection.visibility = View.VISIBLE
        binding.actions.visibility = View.GONE
    }

    private val actionRequestPermission = registerForActivityResult(RequestMultiplePermissions()) {
        handlePermissionSectionVisibility()
    }

    private val actionTakePicture = registerForActivityResult(TakePicture()) { success ->
        if (!success) {
            Log.d(tag, "Image taken FAIL")
            return@registerForActivityResult
        }

        Log.d(tag, "Image taken SUCCESS")

        viewModel.temporaryPhotoUri?.let {
            viewModel.loadCameraMedia(it)
            viewModel.saveTemporarilyPhotoUri(null)
        }
    }

    private val actionTakeVideo = registerForActivityResult(CustomTakeVideo()) { uri ->
        if (uri == null) {
            Log.d(tag, "Video taken FAIL")
            return@registerForActivityResult
        }

        Log.d(tag, "Video taken SUCCESS")
        viewModel.loadCameraMedia(uri)
    }
}
