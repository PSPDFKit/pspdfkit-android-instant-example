/*
 *   Copyright © 2018-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.ui.documentgrid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.pspdfkit.example.instant.R;
import com.pspdfkit.example.instant.api.WebExampleDocumentDescriptor;
import com.pspdfkit.example.instant.api.WebExampleDocumentLayerDescriptor;
import com.pspdfkit.example.instant.ui.InstantKioskGridFragment;
import com.pspdfkit.utils.Size;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Adapter that shows list of web example documents in {@link InstantKioskGridFragment}.
 */
public class DocumentAdapter extends ArrayAdapter<WebExampleDocumentDescriptor> {

    @NonNull private final BitmapDrawable noPreviewDrawable;
    @NonNull private final LruCache<String, Bitmap> previewImageCache;
    @NonNull private final Size previewImageSize;

    @NonNull private final InstantKioskGridFragment kioskGridFragment;

    @NonNull private CompositeDisposable previewRenderDisposables = new CompositeDisposable();

    public DocumentAdapter(@NonNull Context context, @NonNull InstantKioskGridFragment instantKioskGridFragment) {
        super(context, -1);

        this.kioskGridFragment = instantKioskGridFragment;

        previewImageCache = new LruCache<String, Bitmap>((int) ((Runtime.getRuntime().maxMemory() / 1024) / 8)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return value.getByteCount() / 1024;
            }
        };

        previewImageSize = new Size(
            context.getResources().getDimensionPixelSize(R.dimen.kiosk_previewimage_width),
            context.getResources().getDimensionPixelSize(R.dimen.kiosk_previewimage_height)
        );

        noPreviewDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.document);
    }

    /**
     * Sets web example document descriptors to the adapter.
     */
    public void setDocuments(List<WebExampleDocumentDescriptor> documents) {
        clear();
        addAll(documents);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final DocumentViewHolder holder = DocumentViewHolder.get(convertView, parent);
        WebExampleDocumentDescriptor documentDescriptor = getItem(position);
        final WebExampleDocumentLayerDescriptor layerDescriptor = documentDescriptor.getDefaultLayer();

        if (holder.previewRenderDisposable != null) {
            holder.previewRenderDisposable.dispose();
            previewRenderDisposables.delete(holder.previewRenderDisposable);
        }

        // We only want to render a new preview image if we don't already have one in the cache.
        Bitmap cachedPreview = getPreviewFromCache(layerDescriptor);
        holder.itemPreviewImageView.setImageBitmap(cachedPreview != null ? cachedPreview : noPreviewDrawable.getBitmap());
        if (cachedPreview == null) {
            // Calculate the size of the rendered preview image.
            holder.previewRenderDisposable = kioskGridFragment.renderDocumentPreview(layerDescriptor, previewImageSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    holder.itemPreviewImageView.setImageBitmap(bitmap);
                    addPreviewToCache(layerDescriptor, bitmap);
                });
            previewRenderDisposables.add(holder.previewRenderDisposable);
        }

        if (!TextUtils.isEmpty(documentDescriptor.title)) {
            holder.itemTitleView.setText(documentDescriptor.title);
        } else {
            holder.itemTitleView.setText(getContext().getResources().getText(R.string.pspdf__activity_title_unnamed_document));
        }

        return holder.view;
    }

    /**
     * Cancels ongoing rendering of document previews.
     */
    public void cancelPreviewRendering() {
        previewRenderDisposables.clear();
    }

    @Nullable
    private Bitmap getPreviewFromCache(@NonNull WebExampleDocumentLayerDescriptor layerDescriptor) {
        return previewImageCache.get(getPreviewCacheKey(layerDescriptor));
    }

    private void addPreviewToCache(@NonNull WebExampleDocumentLayerDescriptor layerDescriptor, @NonNull Bitmap bitmap) {
        previewImageCache.put(getPreviewCacheKey(layerDescriptor), bitmap);
    }

    /**
     * Invalidates preview for single document layer.
     */
    public void removePreviewFromCache(@NonNull WebExampleDocumentLayerDescriptor layerDescriptor) {
        previewImageCache.remove(getPreviewCacheKey(layerDescriptor));
    }

    @NonNull
    private String getPreviewCacheKey(@NonNull WebExampleDocumentLayerDescriptor layerDescriptor) {
        if (layerDescriptor.layerName == null) {
            return layerDescriptor.documentId;
        } else {
            return layerDescriptor.documentId + "_" + layerDescriptor.layerName;
        }
    }
}
