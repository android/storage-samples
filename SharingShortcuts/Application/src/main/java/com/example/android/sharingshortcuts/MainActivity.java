/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.sharingshortcuts;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provides the landing screen of this sample. There is nothing particularly interesting here. All
 * the codes related to the Direct Share feature are in {@link SharingShortcutsManager}.
 */
public class MainActivity extends Activity {

    // Domain authority for our app FileProvider
    private static final String FILE_PROVIDER_AUTHORITY =
            "com.example.android.sharingshortcuts.fileprovider";

    // Cache directory to store images
    // This is the same path specified in the @xml/file_paths and accessed from the AndroidManifest
    private static final String IMAGE_CACHE_DIR = "images";

    // Name of the file to use for the thumbnail image
    private static final String IMAGE_FILE = "image.png";

    private EditText mEditBody;
    private SharingShortcutsManager mSharingShortcutsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditBody = findViewById(R.id.body);
        findViewById(R.id.share).setOnClickListener(mOnClickListener);

        mSharingShortcutsManager = new SharingShortcutsManager();
        mSharingShortcutsManager.pushDirectShareTargets(this);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.share:
                    share();
                    break;
            }
        }
    };

    /**
     * Emits a sample share {@link Intent}.
     */
    private void share() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mEditBody.getText().toString());
        // (Optional) If you want a preview title, set it with Intent.EXTRA_TITLE
        sharingIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.send_intent_title));

        // (Optional) if you want a preview thumbnail, create a content URI and add it
        // The system only supports content URIs
        ClipData thumbnail = getClipDataThumbnail();
        if (thumbnail != null) {
            sharingIntent.setClipData(thumbnail);
            sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        startActivity(Intent.createChooser(sharingIntent, null));
    }

    /**
     * Get ClipData thumbnail object that needs to be passed in the Intent.
     * It stores the launcher icon in the cache and retrieves in a content URI.
     * The ClipData object is created with the URI we get from the FileProvider.
     * <p>
     * For this to work, you need to configure a FileProvider in the project. We added it to the
     * AndroidManifest.xml file where we can configure it. We added the images path where we
     * save the image to the @xml/file_paths file which tells the FileProvider where we intend to
     * request content URIs.
     *
     * @return thumbnail ClipData object to set in the sharing Intent.
     */
    private ClipData getClipDataThumbnail() {
        try {
            Uri contentUri = saveImageThumbnail();
            return ClipData.newUri(getContentResolver(), null, contentUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save our Launcher image to the cache and return it as a content URI.
     *
     * IMPORTANT: This could trigger StrictMode. Do not do this in your app.
     * For the simplicity of the code sample, this is running on the Main thread
     * but these tasks should be done in a background thread.
     *
     * @throws IOException if image couldn't be saved to the cache.
     * @return image content Uri
     */
    private Uri saveImageThumbnail() throws IOException {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        File cachePath = new File(getCacheDir(), IMAGE_CACHE_DIR);
        cachePath.mkdirs();
        FileOutputStream stream = new FileOutputStream(cachePath + "/" + IMAGE_FILE);
        bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.close();
        File imagePath = new File(getCacheDir(), IMAGE_CACHE_DIR);
        File newFile = new File(imagePath, IMAGE_FILE);
        return FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, newFile);
    }
}
