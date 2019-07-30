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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ViewHolder that represents an image.
 */
class ImageViewHolder extends RecyclerView.ViewHolder {

    ImageView mImageView;
    TextView mTextView;

    ImageViewHolder(View itemView) {
        super(itemView);

        mImageView = itemView.findViewById(R.id.imageview);
        mTextView = itemView.findViewById(R.id.textview_image_label);
    }
}
