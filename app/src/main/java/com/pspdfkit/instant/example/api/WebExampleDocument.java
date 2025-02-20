/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.api;

import androidx.annotation.NonNull;
import java.util.List;

/** Document returned by "GET /api/documents" from web example server. */
class WebExampleDocument {
    @NonNull
    public final String id;

    @NonNull
    public final String title;

    @NonNull
    public final List<String> layers;

    @NonNull
    public final List<String> tokens;

    public WebExampleDocument(
            @NonNull final String id,
            @NonNull final String title,
            @NonNull final List<String> layers,
            @NonNull final List<String> tokens) {
        this.id = id;
        this.title = title;
        this.layers = layers;
        this.tokens = tokens;
    }
}
