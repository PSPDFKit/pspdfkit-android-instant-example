/*
 *   Copyright © 2018-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui.documentgrid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.pspdfkit.instant.example.R;
import io.reactivex.rxjava3.disposables.Disposable;

/** Holder for views displaying web example document previews. */
class DocumentViewHolder {

    @NonNull
    public static DocumentViewHolder get(@Nullable View view, @NonNull final ViewGroup parent) {
        final DocumentViewHolder holder;

        if (view != null) {
            holder = (DocumentViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kiosk_item, parent, false);
            holder = new DocumentViewHolder(view);
            view.setTag(holder);
        }

        return holder;
    }

    @NonNull
    public final View view;

    @NonNull
    public final ImageView itemPreviewImageView;

    @NonNull
    public final TextView itemTitleView;

    @Nullable
    public Disposable previewRenderDisposable;

    private DocumentViewHolder(@NonNull final View view) {
        this.view = view;
        this.itemPreviewImageView = view.findViewById(R.id.itemPreviewImageView);
        this.itemTitleView = view.findViewById(R.id.itemTileView);
    }
}
