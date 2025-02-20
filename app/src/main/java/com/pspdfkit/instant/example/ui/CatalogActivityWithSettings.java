/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.pspdfkit.Nutrient;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.instant.example.R;
import com.pspdfkit.instant.example.utils.Utils;

/** Activity with catalog toolbar and settings sidebar. */
public class CatalogActivityWithSettings extends AppCompatActivity {

    @LayoutRes
    private final int layoutResId;

    private FixedDrawerLayout drawerLayout;
    private View settingsDrawer;

    public CatalogActivityWithSettings(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Replace the initial launcher theme which is specified in the AndroidManifest.xml.
        setTheme(R.style.PSPDFCatalog_Theme_Light);

        final StrictMode.ThreadPolicy oldThreadPolicy = StrictMode.getThreadPolicy();
        final StrictMode.VmPolicy oldVmPolicy = StrictMode.getVmPolicy();

        try {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
            StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX);

            setContentView(layoutResId);
        } finally {
            StrictMode.setThreadPolicy(oldThreadPolicy);
            StrictMode.setVmPolicy(oldVmPolicy);
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();

        if (ab == null) {
            throw new ExceptionInInitializerError(getClass().getSimpleName()
                    + " is missing the ActionBar. Probably the wrong theme has been supplied.");
        }

        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setLogo(R.drawable.ic_logo_padded);

        final SpannableString abTitle;
        final String appLabel = getActivityLabel();
        if (Nutrient.VERSION.startsWith("android-") && Nutrient.VERSION.length() > 8) {
            // Tagged development builds have a slightly different format.
            abTitle = new SpannableString(appLabel + " v" + Nutrient.VERSION.substring(8));
        } else if (Nutrient.VERSION.contains("-")) {
            // Nightly versions have longer version so shorten it.
            String[] split = Nutrient.VERSION.split("-");
            abTitle = new SpannableString(appLabel + " v" + split[0] + "-" + split[split.length - 1]);
        } else {
            abTitle = new SpannableString(appLabel + " v" + Nutrient.VERSION);
        }

        abTitle.setSpan(
                new RelativeSizeSpan(0.75f), appLabel.length(), abTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ab.setTitle(abTitle);

        final Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
        setTaskDescription(new ActivityManager.TaskDescription(
                getString(R.string.app_name), logoBitmap, ContextCompat.getColor(this, R.color.color_primary)));

        // Prepare side drawer
        drawerLayout = findViewById(R.id.main_drawer);
        settingsDrawer = findViewById(R.id.settings_drawer);
        if (settingsDrawer != null) {
            Utils.setProperNavigationDrawerWidth(settingsDrawer);

            // Add the preferences to the drawer
            if (getSupportFragmentManager().findFragmentById(R.id.settings_drawer) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings_drawer, createCatalogPreferencesFragment())
                        .commit();
            }
        }
        // Initialize default preferences.
        CatalogPreferencesFragment.initializeDefaultValues(this);
    }

    @NonNull
    protected CatalogPreferencesFragment createCatalogPreferencesFragment() {
        return new CatalogPreferencesFragment();
    }

    @NonNull
    protected PdfActivityConfiguration.Builder getConfiguration() {
        return CatalogPreferencesFragment.getConfiguration(this);
    }

    @NonNull
    private String getActivityLabel() {
        String appName = "Nutrient";
        PackageManager packageManager = getPackageManager();
        try {
            appName = packageManager
                    .getActivityInfo(getComponentName(), 0)
                    .loadLabel(packageManager)
                    .toString();
        } catch (PackageManager.NameNotFoundException ignored) {
            // Ignored.
        }
        return appName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();

        if (menuItemId == R.id.action_settings) {
            if (drawerLayout == null || settingsDrawer == null) return false;
            if (drawerLayout.isDrawerOpen(settingsDrawer)) {
                drawerLayout.closeDrawer(settingsDrawer);
            } else {
                drawerLayout.openDrawer(settingsDrawer);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // drawerLayout is only used on phones.
        if (drawerLayout != null && drawerLayout.isDrawerOpen(settingsDrawer)) {
            drawerLayout.closeDrawer(settingsDrawer);
        } else {
            super.onBackPressed();
        }
    }
}
