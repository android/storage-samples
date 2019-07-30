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

import com.bumptech.glide.Glide;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for RecyclerView, which manages the image documents.
 */
class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final Context mContext;

    /** Holds the information for already retrieved images. */
    private final List<ImageDocument> mImageDocuments = new ArrayList<>();

    /**
     * The total size of the all images. This number should be the size for all images even if
     * they are not fetched from the ContentProvider.
     */
    private int mTotalSize;

    ImageAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Resources resources = mContext.getResources();
        if (mImageDocuments.size() > position) {
            Glide.with(mContext)
                    .load(mImageDocuments.get(position).mAbsolutePath)
                    .placeholder(R.drawable.cat_placeholder)
                    .into(holder.mImageView);
            holder.mTextView.setText(String.valueOf(position + 1));
        } else {
            holder.mImageView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.cat_placeholder, null));
        }
    }

    /**
     * Add an image as part of the adapter.
     *
     * @param imageDocument the image information to be added
     */
    void add(ImageDocument imageDocument) {
        mImageDocuments.add(imageDocument);
    }

    /**
     * Set the total size of all images.
     *
     * @param totalSize the total size
     */
    void setTotalSize(int totalSize) {
        mTotalSize = totalSize;
    }

    /**
     * @return the number of images already fetched and added to this adapter.
     */
    int getFetchedItemCount() {
        return mImageDocuments.size();
    }

    @Override
    public int getItemCount() {
        return mTotalSize;
    }

    /**
     * Represents information for an image.
     */
    static class ImageDocument {

        String mAbsolutePath;

        String mDisplayName;
    }
}
