/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.ui;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.pspdfkit.example.instant.R;
import com.pspdfkit.example.instant.api.WebExampleClient;
import com.pspdfkit.example.instant.api.WebExampleDocumentDescriptor;
import com.pspdfkit.example.instant.api.WebExampleDocumentLayerDescriptor;
import com.pspdfkit.example.instant.preferences.InstantConnectionPreferences;
import com.pspdfkit.instant.document.InstantPdfDocument;
import com.pspdfkit.instant.exceptions.InstantErrorCode;
import com.pspdfkit.instant.exceptions.InstantException;
import com.pspdfkit.instant.ui.InstantPdfActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity with Instant sync progress indicator.
 */
public class InstantExampleActivity extends InstantPdfActivity {

    public static final String PARAM_DOCUMENT_DESCRIPTOR = "InstantExampleActivity.DocumentDescriptor";

    private WebExampleClient webExampleClient;
    private String webExampleServerUrl;
    private WebExampleDocumentDescriptor documentDescriptor;

    /** True when annotation sync or authentication previously failed with an error. */
    private boolean isError;

    private boolean isAuthenticating = false;

    @ColorInt private int mainToolbarIconsColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPdfFragment().addInstantDocumentListener(this);

        webExampleServerUrl = InstantConnectionPreferences.getInstantServerUrl(this);
        webExampleClient = new WebExampleClient(InstantConnectionPreferences.getWebExampleServerUrl(this), InstantConnectionPreferences.getUserName(this));
        documentDescriptor = getIntent().getParcelableExtra(PARAM_DOCUMENT_DESCRIPTOR);

        final TypedArray a = getTheme().obtainStyledAttributes(
            null,
            R.styleable.pspdf__ActionBarIcons,
            R.attr.pspdf__actionBarIconsStyle,
            R.style.PSPDFKit_ActionBarIcons
        );
        mainToolbarIconsColor = a.getColor(R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor, ContextCompat.getColor(this, R.color.white));
        a.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (documentDescriptor == null) return true;

        MenuItem layersMenuItem = menu.add(0, R.id.layers_button, 0, getString(R.string.layers));
        layersMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (documentDescriptor == null) return true;

        MenuItem layersMenuItem = menu.findItem(R.id.layers_button);
        if (layersMenuItem == null) {
            layersMenuItem = menu.add(0, R.id.layers_button, 0, getString(R.string.layers));
            layersMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        layersMenuItem.setEnabled(getDocument() != null);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_layers);
        drawable.setAlpha(getDocument() != null ? 255 : 128);
        DrawableCompat.setTint(drawable, mainToolbarIconsColor);
        layersMenuItem.setIcon(drawable);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.layers_button) {
            if (documentDescriptor == null) return true;

            View menuItemView = findViewById(R.id.layers_button);
            PopupMenu popupMenu = new PopupMenu(this, menuItemView);

            String currentLayerName = getCurrentLayerName();

            for (final WebExampleDocumentLayerDescriptor layer : documentDescriptor.layers) {
                MenuItem menuItem;
                if (TextUtils.isEmpty(layer.layerName)) {
                    menuItem = popupMenu.getMenu().add(R.string.default_layer);
                    menuItem.setEnabled(!TextUtils.isEmpty(currentLayerName));
                } else {
                    menuItem = popupMenu.getMenu().add(layer.layerName);
                    menuItem.setEnabled(!layer.layerName.equals(currentLayerName));
                }
                menuItem.setOnMenuItemClickListener(item1 -> {
                    showDocumentLayer(layer);
                    return true;
                });
            }
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDocumentLayer(@NonNull WebExampleDocumentLayerDescriptor layer) {
        setDocument(webExampleServerUrl, layer.jwt);
    }

    @Nullable
    private String getCurrentLayerName() {
        InstantPdfDocument document = getDocument();
        if (document == null) return null;
        return document.getInstantDocumentDescriptor().getLayerName();
    }

    @Override
    public void onAuthenticationFailed(@NonNull InstantPdfDocument instantDocument, @NonNull InstantException error) {
        super.onAuthenticationFailed(instantDocument, error);
        notifyInstantError(error);
    }

    @Override
    public void onAuthenticationFinished(@NonNull InstantPdfDocument instantDocument, @NonNull String validJwt) {
        super.onAuthenticationFinished(instantDocument, validJwt);

        // Reset error flag to show error toast on next authentication failure.
        isError = false;
    }

    @Override
    public void onSyncStarted(@NonNull InstantPdfDocument instantDocument) {
        super.onSyncStarted(instantDocument);
    }

    @Override
    public void onSyncError(@NonNull InstantPdfDocument instantDocument, @NonNull InstantException error) {
        super.onSyncError(instantDocument, error);

        if (error.getErrorCode() == InstantErrorCode.AUTHENTICATION_FAILED) {
            // Sync failed due to authentication error. This might typically occur when authentication token has expired.

            // Skip authentication if document is not loaded or when another authentication is in progress.
            final InstantPdfDocument document = getDocument();
            if (document == null || isAuthenticating) return;
            isAuthenticating = true;

            // We will now query web example server for a new authentication token and re-authenticate with Instant server.
            webExampleClient.getJwt(instantDocument.getInstantDocumentDescriptor().getDocumentId())
                .flatMapCompletable(document::reauthenticateWithJwtAsync)
                .subscribeOn(Schedulers.io())
                .doFinally(() -> isAuthenticating = false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // Could not authenticate.
                        notifyAuthenticationError();
                    }
                });
        } else {
            notifyInstantError(error);
        }
    }

    @Override
    public void onSyncFinished(@NonNull InstantPdfDocument instantDocument) {
        super.onSyncFinished(instantDocument);

        // Reset error flag to show error toast on next sync failure.
        isError = false;
    }

    @Override
    public void onDocumentCorrupted(@NonNull InstantPdfDocument instantDocument) {
        super.onDocumentCorrupted(instantDocument);

        // PSPDFKit Instant detected corruption in document data. Document will get invalidated now.
        // Remove local document storage so it can be re-downloaded when opened next time.
        instantDocument.removeLocalStorage();
    }

    @Override
    public void onDocumentInvalidated(@NonNull InstantPdfDocument instantDocument) {
        super.onDocumentInvalidated(instantDocument);

        // Finish the activity when document becomes invalid. This could happen when document storage gets corrupted.
        finish();
    }

    private void notifyInstantError(@NonNull InstantException error) {
        if (!isError &&
            error.getErrorCode() != InstantErrorCode.USER_CANCELLED &&
            error.getErrorCode() != InstantErrorCode.ALREADY_SYNCING) {
            isError = true;

            String errorText = null;
            switch (error.getErrorCode()) {
                case OLD_CLIENT:
                    // Client version is not compatible with server.
                    // You need to update your app to a compatible version and release an update.
                    // If a user sees this on their device they need to update your app.

                    // This is already handled by InstantPdfFragment by showing error dialog.
                    // If you wish to disable this behavior, run
                    //      getPdfFragment().setHandleCriticalInstantErrors(false)
                    // in Activity#onCreate().
                    break;

                case OLD_SERVER:
                    // Server version is not compatible with your client.
                    // The server needs to be updated to a compatible version.
                    // You should update your server before releasing the updated
                    // client to ensure this error is never encountered on users’ devices.
                    errorText = getString(R.string.error_old_server);
                    break;

                default:
                    errorText = getString(R.string.error_syncing_annotations);
            }
            if (errorText != null) {
                Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void notifyAuthenticationError() {
        if (!isError) {
            isError = true;
            Toast.makeText(InstantExampleActivity.this, R.string.error_syncing_annotations_authentication_failed, Toast.LENGTH_LONG).show();
        }
    }
}
