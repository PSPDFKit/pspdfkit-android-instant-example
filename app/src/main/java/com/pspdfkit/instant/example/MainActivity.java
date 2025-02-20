/*
 *   Copyright © 2017-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.instant.client.InstantClient;
import com.pspdfkit.instant.example.api.WebExampleClient;
import com.pspdfkit.instant.example.db.WebExampleDocumentsDatabase;
import com.pspdfkit.instant.example.preferences.InstantConnectionPreferences;
import com.pspdfkit.instant.example.ui.CatalogActivityWithSettings;
import com.pspdfkit.instant.example.ui.CatalogPreferencesFragment;
import com.pspdfkit.instant.example.ui.InstantCatalogPreferencesFragment;
import com.pspdfkit.instant.example.ui.InstantConnectionPreferencesFragment;
import com.pspdfkit.instant.example.ui.InstantKioskGridFragment;
import com.pspdfkit.instant.exceptions.InstantDownloadException;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/** This activity shows list of documents on example server in a grid. */
public class MainActivity extends CatalogActivityWithSettings implements InstantConnectionPreferencesFragment.Callback {

    private boolean isLoggedIn;
    private MenuItem logOutButton;

    /** Disposable for the client connection. */
    @Nullable
    private Disposable webConnectDisposable;

    public MainActivity() {
        super(R.layout.activity_instant_example_main);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLoggedIn = InstantConnectionPreferences.isLoggedIn(this);
        if (isLoggedIn) {
            // Show Kiosk grid with documents on the example server.
            showInstantKioskGridFragment();
        } else {
            // Show connection settings screen.
            showConnectionPreferencesFragment();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webConnectDisposable != null) {
            webConnectDisposable.dispose();
            webConnectDisposable = null;
        }
    }

    @NonNull
    protected CatalogPreferencesFragment createCatalogPreferencesFragment() {
        return new InstantCatalogPreferencesFragment();
    }

    @NonNull
    protected PdfActivityConfiguration.Builder getConfiguration() {
        return InstantCatalogPreferencesFragment.getConfiguration(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final boolean result = super.onCreateOptionsMenu(menu);

        logOutButton = menu.add(0, R.id.log_out_button, 1000, R.string.log_out);
        logOutButton.setIcon(R.drawable.ic_logout);
        logOutButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        refreshLogOutButtonState();

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int menuItemId = item.getItemId();
        if (menuItemId == R.id.log_out_button) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.log_out_confirmation)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        onLogOut();
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setCancelable(true)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoginRequest(@NonNull final String serverUrl, @NonNull final String userName) {
        // Check if we can successfully connect to example server with provided connection
        // preferences.
        final ProgressDialog progressDialog =
                ProgressDialog.show(this, null, getString(R.string.progress_connecting), true, false);
        try {
            final WebExampleClient webExampleClient = new WebExampleClient(serverUrl, userName);
            webConnectDisposable = webExampleClient
                    .getDocumentsAsync()
                    .firstElement()
                    .map(documentDescriptor -> {
                        // Try opening default layer of the first document to see if it's
                        // possible to establish connection to example server.
                        if (documentDescriptor.layers.isEmpty()) {
                            throw new IllegalStateException("Document has no layers!");
                        }
                        final InstantClient instantClient = InstantClient.create(
                                MainActivity.this,
                                InstantConnectionPreferences.getInstantServerUrlFromServerUrl(serverUrl));
                        instantClient.openDocument(documentDescriptor.layers.get(0).jwt);
                        return documentDescriptor;
                    })
                    .ignoreElement()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(progressDialog::dismiss)
                    .subscribe(
                            () -> {
                                InstantConnectionPreferences.setConnectionPreferences(
                                        MainActivity.this, serverUrl, userName);
                                showInstantKioskGridFragment();
                                refreshLogOutButtonState();
                            },
                            this::showErrorToast);
        } catch (final Throwable e) {
            progressDialog.dismiss();
            showErrorToast(e);
            if (webConnectDisposable != null) {
                webConnectDisposable.dispose();
                webConnectDisposable = null;
            }
        }
    }

    private void refreshLogOutButtonState() {
        isLoggedIn = InstantConnectionPreferences.isLoggedIn(this);
        if (logOutButton != null) {
            logOutButton.setEnabled(isLoggedIn);
            logOutButton.setVisible(isLoggedIn);
        }
    }

    private void onLogOut() {
        final String instantServerUrl = InstantConnectionPreferences.getInstantServerUrl(this);
        Completable.fromAction(() -> {
                    // Remove web example storage.
                    final WebExampleDocumentsDatabase webExampleDatabase =
                            new WebExampleDocumentsDatabase(MainActivity.this);
                    webExampleDatabase.removeAllDocuments();

                    // Remove instant client storage. This invalidates all loaded documents.
                    final InstantClient instantClient = InstantClient.create(MainActivity.this, instantServerUrl);
                    instantClient.removeLocalStorage();
                })
                .subscribeOn(Schedulers.io())
                .subscribe();

        // Clear connection preferences
        InstantConnectionPreferences.clearConnectionPreferences(this);

        // Replace kiosk grid fragment with connection preferences fragment.
        showConnectionPreferencesFragment();

        // Hide log out button.
        refreshLogOutButtonState();
    }

    private void showInstantKioskGridFragment() {
        InstantKioskGridFragment kioskGridFragment = (InstantKioskGridFragment)
                getSupportFragmentManager().findFragmentByTag(InstantKioskGridFragment.FRAGMENT_TAG);
        if (kioskGridFragment == null) {
            kioskGridFragment = new InstantKioskGridFragment();
            final FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.fragment_container, kioskGridFragment, InstantKioskGridFragment.FRAGMENT_TAG)
                    .commit();
        }
    }

    private void showConnectionPreferencesFragment() {
        InstantConnectionPreferencesFragment connectionSettingsFragment = (InstantConnectionPreferencesFragment)
                getSupportFragmentManager().findFragmentByTag(InstantConnectionPreferencesFragment.FRAGMENT_TAG);
        if (connectionSettingsFragment == null) {
            connectionSettingsFragment = new InstantConnectionPreferencesFragment();
            final FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(
                            R.id.fragment_container,
                            connectionSettingsFragment,
                            InstantConnectionPreferencesFragment.FRAGMENT_TAG)
                    .commit();
        }
        connectionSettingsFragment.setCallback(this);
    }

    private void showErrorToast(@NonNull final Throwable throwable) {
        String connectionFailedToastMessage = getString(R.string.error_connection_failed);
        if (throwable instanceof InstantDownloadException) {
            final InstantDownloadException downloadException = (InstantDownloadException) throwable;
            switch (downloadException.getErrorCode()) {
                case OLD_CLIENT:
                    connectionFailedToastMessage = getString(R.string.error_old_client, getApplicationName());
                    break;
                case OLD_SERVER:
                    connectionFailedToastMessage = getString(R.string.error_old_server);
                    break;
            }
        }
        Toast.makeText(this, connectionFailedToastMessage, Toast.LENGTH_LONG).show();
    }

    @NonNull
    private CharSequence getApplicationName() {
        return getApplicationInfo().loadLabel(getPackageManager());
    }
}
