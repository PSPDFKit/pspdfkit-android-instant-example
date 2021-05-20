/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.example.instant.R;
import com.pspdfkit.example.instant.api.WebExampleClient;
import com.pspdfkit.example.instant.api.WebExampleDocumentDescriptor;
import com.pspdfkit.example.instant.api.WebExampleDocumentLayerDescriptor;
import com.pspdfkit.example.instant.db.WebExampleDocumentsDatabase;
import com.pspdfkit.example.instant.preferences.InstantConnectionPreferences;
import com.pspdfkit.example.instant.ui.documentgrid.DocumentAdapter;
import com.pspdfkit.instant.client.InstantClient;
import com.pspdfkit.instant.document.InstantPdfDocument;
import com.pspdfkit.instant.ui.InstantPdfActivityIntentBuilder;
import com.pspdfkit.utils.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment showing list of documents from example server.
 */
public class InstantKioskGridFragment extends Fragment {

    public static final String FRAGMENT_TAG = "InstantKioskGridFragment.FRAGMENT_TAG";

    private static final String TAG = "Kiosk";

    @NonNull private CompositeDisposable disposables = new CompositeDisposable();

    private String serverUrl;
    private WebExampleClient webExampleClient;
    private InstantClient instantClient;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private WebExampleDocumentsDatabase webExampleDatabase;

    private DocumentAdapter documentAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        serverUrl = InstantConnectionPreferences.getInstantServerUrl(getContext());
        instantClient = InstantClient.create(getContext(), serverUrl);
        webExampleClient = new WebExampleClient(
            InstantConnectionPreferences.getWebExampleServerUrl(getContext()),
            InstantConnectionPreferences.getUserName(getContext())
        );
        webExampleDatabase = new WebExampleDocumentsDatabase(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_instant_kiosk_grid, container, false);

        final GridView documentGrid = root.findViewById(android.R.id.list);
        documentAdapter = new DocumentAdapter(getContext(), this);
        documentGrid.setAdapter(documentAdapter);
        documentGrid.setOnItemClickListener((parent, view, position, id) -> {
            final WebExampleDocumentDescriptor documentDescriptor = documentAdapter.getItem(position);
            if (documentDescriptor == null) return;

            // Open the touched document.
            final Intent intent = InstantPdfActivityIntentBuilder.fromInstantDocument(
                getContext(),
                serverUrl,
                documentDescriptor.getDefaultLayer().jwt)
                .activityClass(InstantExampleActivity.class)
                .configuration(getConfiguration().build())
                .build();

            intent.putExtra(InstantExampleActivity.PARAM_DOCUMENT_DESCRIPTOR, documentDescriptor);

            startActivity(intent);

            // Remove document preview image from preview image cache.
            documentAdapter.removePreviewFromCache(documentDescriptor.getDefaultLayer());
        });

        progressBar = root.findViewById(android.R.id.progress);

        swipeRefreshLayout = root.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> loadInstantDocuments(documentAdapter, true));

        // Disable swipe to refresh when document grid is scrolled.
        documentGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = true;
                if (documentGrid.getChildCount() > 0) {
                    boolean firstItemVisible = documentGrid.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = documentGrid.getChildAt(0).getTop() >= 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        // Load instant documents on background thread.
        loadInstantDocuments(documentAdapter, false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh previews when returning to the kiosk grid fragment.
        documentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        disposables.clear();
        documentAdapter.cancelPreviewRendering();
    }

    private void loadInstantDocuments(@NonNull final DocumentAdapter documentAdapter, final boolean showError) {
        // Replace documents in document adapter.
        disposables.add(
            // First load cached documents from database.
            Single.fromCallable(() -> webExampleDatabase.getDocuments())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnEvent((documents, throwable) -> {
                    if (throwable == null) {
                        progressBar.setVisibility(View.GONE);
                        documentAdapter.setDocuments(documents);
                    }
                })
                .ignoreElement()
                // Then try to retrieve documents from web example server.
                .andThen(webExampleClient.getDocumentsAsync())
                .toList()
                .flatMap((Function<List<WebExampleDocumentDescriptor>, SingleSource<List<WebExampleDocumentDescriptor>>>) newDocuments -> Single.fromCallable(() -> {
                    // Release not existing documents from local storage.
                    List<WebExampleDocumentDescriptor> oldDocuments = webExampleDatabase.getDocuments();
                    Set<WebExampleDocumentDescriptor> documentsToRemove = new HashSet<>(oldDocuments);
                    documentsToRemove.removeAll(newDocuments);
                    for (WebExampleDocumentDescriptor documentDescriptor : documentsToRemove) {
                        instantClient.removeLocalStorageForDocument(documentDescriptor.documentId);
                    }

                    // Replace documents cached in database with new set downloaded from web example server.
                    webExampleDatabase.replaceDocuments(newDocuments);

                    return newDocuments;
                }))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    // Hide progress indicators.
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .subscribe(documentAdapter::setDocuments, throwable -> {
                    Log.e(TAG, "Error while trying to list all instant documents.", throwable);
                    if (showError) {
                        Toast.makeText(getContext(), R.string.error_listing_documents, Toast.LENGTH_LONG).show();
                    }
                }));
    }

    @NonNull
    protected PdfActivityConfiguration.Builder getConfiguration() {
        return InstantCatalogPreferencesFragment.getConfiguration(getContext());
    }

    @NonNull
    public Single<Bitmap> renderDocumentPreview(@NonNull final WebExampleDocumentLayerDescriptor layerDescriptor, @NonNull final Size previewImageSize) {
        return instantClient.getInstantDocumentDescriptorForJwt(layerDescriptor.jwt)
            .openDocumentAsync(layerDescriptor.jwt)
            .subscribeOn(Schedulers.io())
            .flatMap((Function<InstantPdfDocument, Single<Bitmap>>) document -> {
                Size size = calculateBitmapSize(document, previewImageSize);
                return document.renderPageToBitmapAsync(getContext(), 0, (int) size.width, (int) size.height);
            });
    }

    private Size calculateBitmapSize(PdfDocument document, Size availableSpace) {
        Size pageSize = document.getPageSize(0);
        float ratio;
        if (pageSize.width > pageSize.height) {
            ratio = availableSpace.width / pageSize.width;
        } else {
            ratio = availableSpace.height / pageSize.height;
        }
        return new Size(pageSize.width * ratio, pageSize.height * ratio);
    }
}
