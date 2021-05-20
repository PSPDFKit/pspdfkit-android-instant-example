/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.api;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Descriptor for single document on example server.
 */
public class WebExampleDocumentDescriptor implements Parcelable {
    @NonNull public final String documentId;
    @NonNull public final String title;
    @NonNull public final List<WebExampleDocumentLayerDescriptor> layers;

    public WebExampleDocumentDescriptor(@NonNull String documentId,
                                        @NonNull String title,
                                        @NonNull List<WebExampleDocumentLayerDescriptor> layers) {
        if (layers.isEmpty()) {
            throw new IllegalArgumentException("Layers may not be empty.");
        }
        this.documentId = documentId;
        this.title = title;
        this.layers = layers;
    }

    /**
     * Returns default (first) layer.
     */
    @NonNull
    public WebExampleDocumentLayerDescriptor getDefaultLayer() {
        return layers.get(0);
    }

    @Override
    public int hashCode() {
        return documentId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebExampleDocumentDescriptor)) return false;

        return documentId.equals(((WebExampleDocumentDescriptor) o).documentId);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.documentId);
        dest.writeString(this.title);
        Parcelable[] parcelableLayers = new Parcelable[this.layers.size()];
        for (int i = 0; i < this.layers.size(); i++) {
            parcelableLayers[i] = this.layers.get(i);
        }
        dest.writeParcelableArray(parcelableLayers, flags);
    }

    protected WebExampleDocumentDescriptor(Parcel in) {
        this.documentId = in.readString();
        this.title = in.readString();
        Parcelable[] parcelables = in.readParcelableArray(WebExampleDocumentLayerDescriptor.class.getClassLoader());
        this.layers = new ArrayList<>(parcelables.length);
        for (Parcelable parcelable : parcelables) {
            this.layers.add((WebExampleDocumentLayerDescriptor) parcelable);
        }
    }

    public static final Creator<WebExampleDocumentDescriptor> CREATOR = new Creator<WebExampleDocumentDescriptor>() {
        @Override
        public WebExampleDocumentDescriptor createFromParcel(Parcel source) {
            return new WebExampleDocumentDescriptor(source);
        }

        @Override
        public WebExampleDocumentDescriptor[] newArray(int size) {
            return new WebExampleDocumentDescriptor[size];
        }
    };
}
