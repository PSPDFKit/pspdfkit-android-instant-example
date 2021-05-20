/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.api;

import androidx.annotation.NonNull;

/**
 * Result of "GET /api/document/:id".
 */
class WebExampleDocumentAuthenticationResult {

    public final boolean success;
    @NonNull public final String token;

    public WebExampleDocumentAuthenticationResult(boolean success, @NonNull String token) {
        this.success = success;
        this.token = token;
    }
}
