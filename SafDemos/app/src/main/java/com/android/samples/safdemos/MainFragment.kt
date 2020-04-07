/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.samples.safdemos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.samples.safdemos.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(layoutInflater)

        val demoItems = arrayOf(
            // TODO: Add other SAF demos
            Demo("Media picker", R.id.action_mainFragment_to_mediaPickerFragment)
        )

        activity?.let { activity ->
            val adapter = ArrayAdapter(activity.baseContext, android.R.layout.simple_list_item_1, demoItems)
            binding.demosList.adapter = adapter

            binding.demosList.setOnItemClickListener { _, _, position, _ ->
                adapter.getItem(position)?.let {
                    findNavController().navigate(it.action)
                }
            }
        }

        return binding.root
    }
}

data class Demo(val label: String, @IdRes val action: Int) {
    override fun toString(): String = label
}