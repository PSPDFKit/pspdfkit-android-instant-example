/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui;

import static com.pspdfkit.instant.example.aia.AiAssistantInstantHelper.createAiAssistantForInstant;

import android.content.res.TypedArray;
import android.graphics.RectF;
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
import com.pspdfkit.instant.document.InstantPdfDocument;
import com.pspdfkit.instant.example.R;
import com.pspdfkit.instant.example.api.WebExampleClient;
import com.pspdfkit.instant.example.api.WebExampleDocumentDescriptor;
import com.pspdfkit.instant.example.api.WebExampleDocumentLayerDescriptor;
import com.pspdfkit.instant.example.preferences.InstantConnectionPreferences;
import com.pspdfkit.instant.example.utils.JwtGenerator;
import com.pspdfkit.instant.exceptions.InstantErrorCode;
import com.pspdfkit.instant.exceptions.InstantException;
import com.pspdfkit.instant.ui.InstantPdfActivity;
import io.nutrient.domain.ai.AiAssistant;
import io.nutrient.domain.ai.AiAssistantProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/** Activity with Instant sync progress indicator. */
public class InstantExampleActivity extends InstantPdfActivity implements AiAssistantProvider {

    private final String sessionId = "my-test-session-id-11";

    public static final String PARAM_DOCUMENT_DESCRIPTOR = "InstantExampleActivity.DocumentDescriptor";
    public static final String PARAM_ALL_DOCUMENT_DESCRIPTORS = "InstantExampleActivity.AllDocumentDescriptors";

    private WebExampleClient webExampleClient;
    private String webExampleServerUrl;
    private WebExampleDocumentDescriptor currentDocumentDescriptor;
    private ArrayList<WebExampleDocumentDescriptor> allDocsDescriptors;

    /** True when annotation sync or authentication previously failed with an error. */
    private boolean isError;

    private boolean isAuthenticating = false;

    @ColorInt
    private int mainToolbarIconsColor;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPdfFragment().addInstantDocumentListener(this);

        webExampleServerUrl = InstantConnectionPreferences.getInstantServerUrl(this);

        webExampleClient = new WebExampleClient(
                InstantConnectionPreferences.getWebExampleServerUrl(this),
                InstantConnectionPreferences.getUserName(this));
        currentDocumentDescriptor = getIntent().getParcelableExtra(PARAM_DOCUMENT_DESCRIPTOR);
        allDocsDescriptors = getIntent().getParcelableArrayListExtra(PARAM_ALL_DOCUMENT_DESCRIPTORS);

        final TypedArray a = getTheme()
                .obtainStyledAttributes(
                        null,
                        com.pspdfkit.R.styleable.pspdf__ActionBarIcons,
                        com.pspdfkit.R.attr.pspdf__actionBarIconsStyle,
                        com.pspdfkit.R.style.PSPDFKit_ActionBarIcons);
        mainToolbarIconsColor = a.getColor(
                com.pspdfkit.R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor,
                ContextCompat.getColor(this, R.color.white));
        a.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (currentDocumentDescriptor == null) return true;

        final MenuItem layersMenuItem = menu.add(0, R.id.layers_button, 0, getString(R.string.layers));
        layersMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (currentDocumentDescriptor == null) return true;

        MenuItem layersMenuItem = menu.findItem(R.id.layers_button);
        if (layersMenuItem == null) {
            layersMenuItem = menu.add(0, R.id.layers_button, 0, getString(R.string.layers));
            layersMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        layersMenuItem.setEnabled(getDocument() != null);

        final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_layers);
        if (drawable != null) {
            drawable.setAlpha(getDocument() != null ? 255 : 128);
            DrawableCompat.setTint(drawable, mainToolbarIconsColor);
            layersMenuItem.setIcon(drawable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.layers_button) {
            if (currentDocumentDescriptor == null) return true;

            final View menuItemView = findViewById(R.id.layers_button);
            final PopupMenu popupMenu = new PopupMenu(this, menuItemView);

            final String currentLayerName = getCurrentLayerName();

            for (final WebExampleDocumentLayerDescriptor layer : currentDocumentDescriptor.layers) {
                final MenuItem menuItem;
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

    private void showDocumentLayer(@NonNull final WebExampleDocumentLayerDescriptor layer) {
        setDocument(webExampleServerUrl, layer.jwt);
    }

    @Nullable
    private String getCurrentLayerName() {
        final InstantPdfDocument document = getDocument();

        if (document == null) return null;
        return document.getInstantDocumentDescriptor().getLayerName();
    }

    @Override
    public void onAuthenticationFailed(
            @NonNull final InstantPdfDocument instantDocument, @NonNull final InstantException error) {
        super.onAuthenticationFailed(instantDocument, error);
        notifyInstantError(error);
    }

    @Override
    public void onAuthenticationFinished(
            @NonNull final InstantPdfDocument instantDocument, @NonNull final String validJwt) {
        super.onAuthenticationFinished(instantDocument, validJwt);

        // Reset error flag to show error toast on next authentication failure.
        isError = false;
    }

    @Override
    public void onSyncStarted(@NonNull final InstantPdfDocument instantDocument) {
        super.onSyncStarted(instantDocument);
    }

    @Override
    public void onSyncError(@NonNull final InstantPdfDocument instantDocument, @NonNull final InstantException error) {
        super.onSyncError(instantDocument, error);

        if (error.getErrorCode() == InstantErrorCode.AUTHENTICATION_FAILED) {
            // Sync failed due to authentication error. This might typically occur when
            // authentication token has expired.

            // Skip authentication if document is not loaded or when another authentication is in
            // progress.
            final InstantPdfDocument document = getDocument();
            if (document == null || isAuthenticating) return;
            isAuthenticating = true;

            // We will now query web example server for a new authentication token and
            // re-authenticate with Instant Server (Nutrient Document Engine).
            webExampleClient
                    .getJwt(instantDocument.getInstantDocumentDescriptor().getDocumentId())
                    .flatMapCompletable(document::reauthenticateWithJwtAsync)
                    .subscribeOn(Schedulers.io())
                    .doFinally(() -> isAuthenticating = false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableCompletableObserver() {
                        @Override
                        public void onComplete() {}

                        @Override
                        public void onError(@NonNull final Throwable e) {
                            // Could not authenticate.
                            notifyAuthenticationError();
                        }
                    });
        } else {
            notifyInstantError(error);
        }
    }

    @Override
    public void onSyncFinished(@NonNull final InstantPdfDocument instantDocument) {
        super.onSyncFinished(instantDocument);

        // Reset error flag to show error toast on next sync failure.
        isError = false;
    }

    @Override
    public void onDocumentCorrupted(@NonNull final InstantPdfDocument instantDocument) {
        super.onDocumentCorrupted(instantDocument);

        // Nutrient Instant detected corruption in document data. Document will get invalidated now.
        // Remove local document storage so it can be re-downloaded when opened next time.
        instantDocument.removeLocalStorage();
    }

    @Override
    public void onDocumentInvalidated(@NonNull final InstantPdfDocument instantDocument) {
        super.onDocumentInvalidated(instantDocument);

        // Finish the activity when document becomes invalid. This could happen when document
        // storage gets corrupted.
        finish();
    }

    private void notifyInstantError(@NonNull final InstantException error) {
        if (!isError
                && error.getErrorCode() != InstantErrorCode.USER_CANCELLED
                && error.getErrorCode() != InstantErrorCode.ALREADY_SYNCING) {
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
            Toast.makeText(
                            InstantExampleActivity.this,
                            R.string.error_syncing_annotations_authentication_failed,
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    @NonNull
    @Override
    public AiAssistant getAiAssistant() {
        return createAiAssistantForInstant(
                this, webExampleServerUrl, allDocsDescriptors, "192.168.1.221", sessionId, (instantDocumentIds) -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("document_ids", instantDocumentIds);
                    claims.put("session_ids", List.of(sessionId));
                    Map<String, Integer> requestLimit = new HashMap<>();
                    requestLimit.put("requests", 160);
                    requestLimit.put("time_period_s", 1000 * 60);
                    claims.put("request_limit", requestLimit);
                    return new JwtGenerator(this).generateJwtToken(claims, "keys/jwt.pem");
                });
    }

    @Override
    public void navigateTo(@NotNull List<? extends @NotNull RectF> documentRect, int pageIndex, int documentIndex) {
        getPdfFragment().highlight(this, new ArrayList(documentRect), pageIndex);
    }
}
