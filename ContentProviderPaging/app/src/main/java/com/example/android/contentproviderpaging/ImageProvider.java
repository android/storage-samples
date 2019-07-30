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

package com.example.android.contentproviderpaging;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * ContentProvider that demonstrates how the paging support works introduced in Android O.
 * This class fetches the images from the local storage but the storage could be
 * other locations such as a remote server.
 */
public class ImageProvider extends ContentProvider {

    private static final String TAG = "ImageDocumentsProvider";

    private static final int IMAGES = 1;

    private static final int IMAGE_ID = 2;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ImageContract.AUTHORITY, "images", IMAGES);
        sUriMatcher.addURI(ImageContract.AUTHORITY, "images/#", IMAGE_ID);
    }

    // Indicated how many same images are going to be written as dummy images
    private static final int REPEAT_COUNT_WRITE_FILES = 10;

    private File mBaseDir;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");

        Context context = getContext();
        if (context == null) {
            return false;
        }
        mBaseDir = context.getFilesDir();
        writeDummyFilesToStorage(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
            @Nullable String[] strings1, @Nullable String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, Bundle queryArgs,
            CancellationSignal cancellationSignal) {
        int match = sUriMatcher.match(uri);
        // We only support a query for multiple images, return null for other form of queries
        // including a query for a single image.
        switch (match) {
            case IMAGES:
                break;
            default:
                return null;
        }
        MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        File[] files = mBaseDir.listFiles();
        int offset = queryArgs.getInt(ContentResolver.QUERY_ARG_OFFSET, 0);
        int limit = queryArgs.getInt(ContentResolver.QUERY_ARG_LIMIT, Integer.MAX_VALUE);
        Log.d(TAG, "queryChildDocuments with Bundle, Uri: " +
                uri + ", offset: " + offset + ", limit: " + limit);
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than 0");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must not be less than 0");
        }

        if (offset >= files.length) {
            return result;
        }

        for (int i = offset, maxIndex = Math.min(offset + limit, files.length); i < maxIndex; i++) {
            includeFile(result, files[i]);
        }

        Bundle bundle = new Bundle();
        bundle.putInt(ContentResolver.EXTRA_SIZE, files.length);
        String[] honoredArgs = new String[2];
        int size = 0;
        if (queryArgs.containsKey(ContentResolver.QUERY_ARG_OFFSET)) {
            honoredArgs[size++] = ContentResolver.QUERY_ARG_OFFSET;
        }
        if (queryArgs.containsKey(ContentResolver.QUERY_ARG_LIMIT)) {
            honoredArgs[size++] = ContentResolver.QUERY_ARG_LIMIT;
        }
        if (size != honoredArgs.length) {
            honoredArgs = Arrays.copyOf(honoredArgs, size);
        }
        bundle.putStringArray(ContentResolver.EXTRA_HONORED_ARGS, honoredArgs);
        result.setExtras(bundle);
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case IMAGES:
                return "vnd.android.cursor.dir/images";
            case IMAGE_ID:
                return "vnd.android.cursor.item/images";
            default:
                throw new IllegalArgumentException(String.format("Unknown URI: %s", uri));
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
            @Nullable String[] strings) {
        throw new UnsupportedOperationException();
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : ImageContract.PROJECTION_ALL;
    }

    /**
     * Add a representation of a file to a cursor.
     *
     * @param result the cursor to modify
     * @param file   the File object representing the desired file (may be null if given docID)
     */
    private void includeFile(MatrixCursor result, File file) {
        MatrixCursor.RowBuilder row = result.newRow();
        row.add(ImageContract.Columns.DISPLAY_NAME, file.getName());
        row.add(ImageContract.Columns.SIZE, file.length());
        row.add(ImageContract.Columns.ABSOLUTE_PATH, file.getAbsolutePath());
    }

    /**
     * Preload sample files packaged in the apk into the internal storage directory.  This is a
     * dummy function specific to this demo.  The MyCloud mock cloud service doesn't actually
     * have a backend, so it simulates by reading content from the device's internal storage.
     */
    private void writeDummyFilesToStorage(Context context) {
        if (mBaseDir.list().length > 0) {
            return;
        }

        int[] imageResIds = getResourceIdArray(context, R.array.image_res_ids);
        for (int i = 0; i < REPEAT_COUNT_WRITE_FILES; i++) {
            for (int resId : imageResIds) {
                writeFileToInternalStorage(context, resId, "-" + i + ".jpeg");
            }
        }
    }

    /**
     * Write a file to internal storage.  Used to set up our dummy "cloud server".
     *
     * @param context   the Context
     * @param resId     the resource ID of the file to write to internal storage
     * @param extension the file extension (ex. .png, .mp3)
     */
    private void writeFileToInternalStorage(Context context, int resId, String extension) {
        InputStream ins = context.getResources().openRawResource(resId);
        int size;
        byte[] buffer = new byte[1024];
        try {
            String filename = context.getResources().getResourceEntryName(resId) + extension;
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            while ((size = ins.read(buffer, 0, 1024)) >= 0) {
                fos.write(buffer, 0, size);
            }
            ins.close();
            fos.write(buffer);
            fos.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int[] getResourceIdArray(Context context, int arrayResId) {
        TypedArray ar = context.getResources().obtainTypedArray(arrayResId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        return resIds;
    }
}
