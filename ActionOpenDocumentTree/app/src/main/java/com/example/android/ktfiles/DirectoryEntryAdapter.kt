/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.ktfiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DirectoryEntryAdapter(
    private val clickListeners: ClickListeners
) : RecyclerView.Adapter<DirectoryEntryAdapter.ViewHolder>() {

    private val directoryEntries = mutableListOf<CachingDocumentFile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.directory_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        with(viewHolder) {
            val item = directoryEntries[position]
            val itemDrawableRes = if (item.isDirectory) {
                R.drawable.ic_folder_black_24dp
            } else {
                R.drawable.ic_file_black_24dp
            }

            fileName.text = item.name
            mimeType.text = item.type ?: ""
            imageView.setImageResource(itemDrawableRes)

            root.setOnClickListener {
                clickListeners.onDocumentClicked(item)
            }
            root.setOnLongClickListener {
                clickListeners.onDocumentLongClicked(item)
                true
            }
        }
    }

    override fun getItemCount() = directoryEntries.size

    fun setEntries(newList: List<CachingDocumentFile>) {
        synchronized(directoryEntries) {
            directoryEntries.clear()
            directoryEntries.addAll(newList)
            notifyDataSetChanged()
        }
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val fileName: TextView = view.findViewById(R.id.file_name)
        val mimeType: TextView = view.findViewById(R.id.mime_type)
        val imageView: ImageView = view.findViewById(R.id.entry_image)
    }
}

interface ClickListeners {
    fun onDocumentClicked(clickedDocument: CachingDocumentFile)
    fun onDocumentLongClicked(clickedDocument: CachingDocumentFile)
}
