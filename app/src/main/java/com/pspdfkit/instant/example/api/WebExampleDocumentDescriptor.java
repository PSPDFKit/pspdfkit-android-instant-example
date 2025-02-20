/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.api;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/** Descriptor for single document on example server. */
public class WebExampleDocumentDescriptor implements Parcelable {
    @NonNull
    public final String documentId;

    @NonNull
    public final String title;

    @NonNull
    public final List<WebExampleDocumentLayerDescriptor> layers;

    public WebExampleDocumentDescriptor(
            @NonNull final String documentId,
            @NonNull final String title,
            @NonNull final List<WebExampleDocumentLayerDescriptor> layers) {
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("Layers may not be empty.");
        }
        this.documentId = documentId;
        this.title = title;
        this.layers = layers;
    }

    /** Returns default (first) layer. */
    @NonNull
    public WebExampleDocumentLayerDescriptor getDefaultLayer() {
        return layers.get(0);
    }

    @Override
    public int hashCode() {
        return documentId.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WebExampleDocumentDescriptor)) return false;

        return documentId.equals(((WebExampleDocumentDescriptor) o).documentId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(this.documentId);
        dest.writeString(this.title);
        final Parcelable[] parcelableLayers = new Parcelable[this.layers.size()];
        for (int i = 0; i < this.layers.size(); i++) {
            parcelableLayers[i] = this.layers.get(i);
        }
        dest.writeParcelableArray(parcelableLayers, flags);
    }

    protected WebExampleDocumentDescriptor(final Parcel in) {
        this.documentId = in.readString();
        this.title = in.readString();
        final Parcelable[] parcelables =
                in.readParcelableArray(WebExampleDocumentLayerDescriptor.class.getClassLoader());
        this.layers = new ArrayList<>(parcelables.length);
        for (final Parcelable parcelable : parcelables) {
            this.layers.add((WebExampleDocumentLayerDescriptor) parcelable);
        }
    }

    public static final Creator<WebExampleDocumentDescriptor> CREATOR = new Creator<>() {
        @Override
        public WebExampleDocumentDescriptor createFromParcel(final Parcel source) {
            return new WebExampleDocumentDescriptor(source);
        }

        @Override
        public WebExampleDocumentDescriptor[] newArray(final int size) {
            return new WebExampleDocumentDescriptor[size];
        }
    };
}
