/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.api;

import android.text.TextUtils;
import android.util.Base64;
import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.reactivestreams.Publisher;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for web example API.
 */
public class WebExampleClient {

    @NonNull private final Retrofit retrofit;
    @NonNull private final String authorization;

    public WebExampleClient(@NonNull String serverUrl, @NonNull String userName) {
        // In this example, we authorize using basic authorization with base64 encoded user name.
        this.authorization = "Basic " + new String(Base64.encode(String.format("%s:", userName).getBytes(), Base64.NO_WRAP));

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build();

        retrofit = new Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }

    @NonNull
    public Flowable<WebExampleDocumentDescriptor> getDocumentsAsync() {
        final WebExampleRetrofitService service = retrofit.create(WebExampleRetrofitService.class);

        // We will execute 2 REST queries here:
        //       GET /api/documents - to download list of available documents.
        //       GET /api/document/:id - to download authentication token for each document.
        return service.getDocuments(authorization)
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .flatMapIterable((Function<WebExampleDocumentList, Iterable<WebExampleDocument>>) webExampleDocumentList -> webExampleDocumentList.documents)
            .flatMap((Function<WebExampleDocument, Publisher<WebExampleDocumentDescriptor>>) webExampleDocument -> service.getJwt(authorization, webExampleDocument.id)
                .toFlowable()
                .filter(webExampleDocumentAuthenticationResult -> webExampleDocumentAuthenticationResult.success)
                .map(webExampleDocumentAuthenticationResult -> {
                    List<String> layers = webExampleDocument.layers;
                    List<String> layersAuthTokens = webExampleDocument.tokens;
                    List<WebExampleDocumentLayerDescriptor> documentLayers = new ArrayList<>(layers.size());

                    // Default layer.
                    documentLayers.add(new WebExampleDocumentLayerDescriptor(webExampleDocument.id, "", webExampleDocumentAuthenticationResult.token));

                    // Other layers.
                    if (layers.size() == layersAuthTokens.size()) {
                        for (int i = 0; i < layers.size(); i++) {
                            if (!TextUtils.isEmpty(layers.get(i))) {
                                documentLayers.add(new WebExampleDocumentLayerDescriptor(
                                    webExampleDocument.id,
                                    layers.get(i),
                                    layersAuthTokens.get(i)
                                ));
                            }
                        }
                    }

                    return new WebExampleDocumentDescriptor(webExampleDocument.id, webExampleDocument.title, documentLayers);
                }));
    }

    /**
     * Retrieves authentication token for Instant document from example web server.
     * @return Single returning authentication token or error if token cold not be retrieved. Scheduled on {@link Schedulers#io()}.
     */
    @NonNull
    public Single<String> getJwt(@NonNull final String documentId) {
        final WebExampleRetrofitService service = retrofit.create(WebExampleRetrofitService.class);
        return service.getJwt(authorization, documentId)
            .subscribeOn(Schedulers.io())
            .toFlowable()
            .filter(webExampleDocumentAuthenticationResult -> webExampleDocumentAuthenticationResult.success)
            .map(webExampleDocumentAuthenticationResult -> webExampleDocumentAuthenticationResult.token).singleOrError();
    }
}
