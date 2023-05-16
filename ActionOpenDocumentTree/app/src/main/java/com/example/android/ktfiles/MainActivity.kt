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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.android.ktfiles.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    /**
     * Replaces the old startActivityForResult approach.
     */
    private val directoryPicker = registerForActivityResult(DirectoryBrowser()){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val openDirectoryButton = binding.fabOpenDirectory
        openDirectoryButton.setOnClickListener {
            openDirectory()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val directoryOpen = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.let { actionBar ->
                actionBar.setDisplayHomeAsUpEnabled(directoryOpen)
                actionBar.setDisplayShowHomeEnabled(directoryOpen)
            }

            if (directoryOpen) {
                openDirectoryButton.visibility = View.GONE
            } else {
                openDirectoryButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return false
    }

    fun showDirectoryContents(directoryUri: Uri) {
        supportFragmentManager.commit {
            val directoryTag = directoryUri.toString()
            val directoryFragment = DirectoryFragment.newInstance(directoryUri)
            replace(R.id.fragment_container, directoryFragment, directoryTag)
            addToBackStack(directoryTag)
        }
    }

    private fun openDirectory() {
        directoryPicker.launch(0)
    }

    inner class DirectoryBrowser : ActivityResultContract<Int, Uri?>() {
        override fun createIntent(context: Context, requestCode: Int) =
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        override fun parseResult(resultCode: Int, result: Intent?) : Uri? {
            if (resultCode != Activity.RESULT_OK) {
                return null
            }

            result?.let{
                val directoryUri = result.data as Uri

                this@MainActivity.contentResolver.takePersistableUriPermission(
                    directoryUri,

                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                showDirectoryContents(directoryUri)

            }

            return result?.data
        }
    }
}
