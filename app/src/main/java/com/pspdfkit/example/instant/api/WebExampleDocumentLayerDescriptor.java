/*
 *   Copyright © 2018-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.api;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Descriptor for a single document layer on example server.
 */
public class WebExampleDocumentLayerDescriptor implements Parcelable {

    @NonNull public final String documentId;
    @Nullable public final String layerName;
    @NonNull public final String jwt;

    public WebExampleDocumentLayerDescriptor(@NonNull String documentId, @Nullable String layerName, @NonNull String jwt) {
        this.documentId = documentId;
        this.layerName = layerName;
        this.jwt = jwt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.documentId);
        dest.writeString(this.layerName);
        dest.writeString(this.jwt);
    }

    protected WebExampleDocumentLayerDescriptor(Parcel in) {
        this.documentId = in.readString();
        this.layerName = in.readString();
        this.jwt = in.readString();
    }

    public static final Creator<WebExampleDocumentLayerDescriptor> CREATOR = new Creator<WebExampleDocumentLayerDescriptor>() {
        @Override
        public WebExampleDocumentLayerDescriptor createFromParcel(Parcel source) {
            return new WebExampleDocumentLayerDescriptor(source);
        }

        @Override
        public WebExampleDocumentLayerDescriptor[] newArray(int size) {
            return new WebExampleDocumentLayerDescriptor[size];
        }
    };
}
