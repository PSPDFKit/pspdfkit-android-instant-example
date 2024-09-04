/*
 *   Copyright © 2017-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.api;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/** Retrofit client for Web example API. */
interface WebExampleRetrofitService {

    @GET("/api/documents")
    Single<WebExampleDocumentList> getDocuments(@Header("Authorization") String userAuthorization);

    @GET("/api/document/{documentId}")
    Single<WebExampleDocumentAuthenticationResult> getJwt(
            @Header("Authorization") String userAuthorization, @Path("documentId") String documentId);
}
