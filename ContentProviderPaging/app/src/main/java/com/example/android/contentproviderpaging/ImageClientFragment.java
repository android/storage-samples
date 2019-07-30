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

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that works as a client for accessing the DocumentsProvider
 * ({@link ImageProvider}.
 */
public class ImageClientFragment extends Fragment {

    private static final String TAG = "ImageClientFragment";

    /** The number of fetched images in a single query to the DocumentsProvider. */
    private static final int LIMIT = 10;

    private ImageAdapter mAdapter;

    private LinearLayoutManager mLayoutManager;

    private final LoaderCallback mLoaderCallback = new LoaderCallback();

    /**
     * The offset position for the ContentProvider to be used as a starting position to fetch
     * the images from.
     */
    private AtomicInteger mOffset = new AtomicInteger(0);

    public static ImageClientFragment newInstance() {

        Bundle args = new Bundle();

        ImageClientFragment fragment = new ImageClientFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_client, container, false);
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        final Activity activity = getActivity();

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerview);
        if (mLayoutManager == null) {
            mLayoutManager = new LinearLayoutManager(activity);
        }
        recyclerView.setLayoutManager(mLayoutManager);
        if (mAdapter == null) {
            mAdapter = new ImageAdapter(activity);
        }
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int lastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition >= mAdapter.getFetchedItemCount()) {
                    Log.d(TAG,
                            "Fetch new images. LastVisiblePosition: " + lastVisiblePosition
                                    + ", NonEmptyItemCount: " + mAdapter.getFetchedItemCount());

                    int pageId = lastVisiblePosition / LIMIT;
                    // Fetch new images once the last fetched item becomes visible
                    activity.getLoaderManager()
                            .restartLoader(pageId, null, mLoaderCallback);
                }
            }
        });

        final Button showButton = rootView.findViewById(R.id.button_show);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.getLoaderManager().restartLoader(0, null, mLoaderCallback);
                showButton.setVisibility(View.GONE);
            }
        });
    }

    private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final Activity activity = ImageClientFragment.this.getActivity();
            return new CursorLoader(activity) {
                @Override
                public Cursor loadInBackground() {
                    Bundle bundle = new Bundle();
                    bundle.putInt(ContentResolver.QUERY_ARG_OFFSET, mOffset.intValue());
                    bundle.putInt(ContentResolver.QUERY_ARG_LIMIT, LIMIT);
                    return activity.getContentResolver()
                            .query(ImageContract.CONTENT_URI, null, bundle, null);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Bundle extras = cursor.getExtras();
            int totalSize = extras.getInt(ContentResolver.EXTRA_SIZE);
            mAdapter.setTotalSize(totalSize);
            int beforeCount = mAdapter.getFetchedItemCount();
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndex(
                        ImageContract.Columns.DISPLAY_NAME));
                String absolutePath = cursor.getString(cursor.getColumnIndex(
                        ImageContract.Columns.ABSOLUTE_PATH));

                ImageAdapter.ImageDocument imageDocument = new ImageAdapter.ImageDocument();
                imageDocument.mAbsolutePath = absolutePath;
                imageDocument.mDisplayName = displayName;
                mAdapter.add(imageDocument);
            }
            int cursorCount = cursor.getCount();
            if (cursorCount == 0) {
                return;
            }
            Activity activity = ImageClientFragment.this.getActivity();
            mAdapter.notifyItemRangeChanged(beforeCount, cursorCount);
            int offsetSnapShot = mOffset.get();
            String message = activity.getResources()
                    .getString(R.string.fetched_images_out_of, offsetSnapShot + 1,
                            offsetSnapShot + cursorCount, totalSize);
            mOffset.addAndGet(cursorCount);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
