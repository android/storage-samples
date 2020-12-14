/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.samples.storage.saf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.samples.storage.Action
import com.samples.storage.ActionListAdapter
import com.samples.storage.R
import com.samples.storage.databinding.FragmentListBinding

private val demoList = arrayOf(
    Action("Add Text File", R.id.action_safFragment_to_addTextFileFragment),
    Action("Edit Text File", R.id.action_safFragment_to_editTextFileFragment),
    Action("Read PDF File", R.id.action_safFragment_to_readPdfFileFragment),
    Action("Get Folder Children", R.id.action_safFragment_to_getFolderChildrenFragment),
)

class SafFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ActionListAdapter(demoList)

        binding.recyclerView

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}