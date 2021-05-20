/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.ui;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
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

import com.pspdfkit.PSPDFKit;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.example.R;
import com.pspdfkit.example.utils.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * Activity with catalog toolbar and settings sidebar.
 */
public class CatalogActivityWithSettings extends AppCompatActivity {

    @LayoutRes private final int layoutResId;

    private FixedDrawerLayout drawerLayout;
    private View settingsDrawer;

    public CatalogActivityWithSettings(int layoutResId) {
        this.layoutResId = layoutResId;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Replace the initial launcher theme which is specified in the AndroidManifest.xml.
        setTheme(R.style.PSPDFCatalog_Theme_Light);

        setContentView(layoutResId);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();

        if (ab == null) {
            throw new ExceptionInInitializerError(getClass().getSimpleName() + " is missing the ActionBar. Probably the wrong theme has been supplied.");
        }

        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayUseLogoEnabled(true);
        ab.setLogo(R.drawable.ic_logo_padded);

        SpannableString abTitle;
        String appLabel = getActivityLabel();
        if (PSPDFKit.VERSION.startsWith("android-") && PSPDFKit.VERSION.length() > 8) {
            // Tagged development builds have a slightly different format.
            abTitle = new SpannableString(appLabel + " v" + PSPDFKit.VERSION.substring(8));
        } else if (PSPDFKit.VERSION.contains("-")) {
            // Nightly versions have longer version so shorten it.
            String[] split = PSPDFKit.VERSION.split("-");
            abTitle = new SpannableString(appLabel + " v" + split[0] + "-" + split[split.length - 1]);
        } else {
            abTitle = new SpannableString(appLabel + " v" + PSPDFKit.VERSION);
        }

        abTitle.setSpan(new RelativeSizeSpan(0.75f), appLabel.length(), abTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ab.setTitle(abTitle);

        // Provide the styling for Lollipop's Recents app.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
            setTaskDescription(new ActivityManager.TaskDescription(
                                   getString(R.string.app_name),
                                   logoBitmap,
                                   ContextCompat.getColor(this, R.color.color_primary)
                               )
            );
        }

        // Prepare KitKat tint
        SystemBarTintManager stm = new SystemBarTintManager(this);
        stm.setStatusBarTintEnabled(true);
        stm.setStatusBarTintResource(R.color.color_primary_dark);

        // Prepare side drawer
        drawerLayout = findViewById(R.id.main_drawer);
        settingsDrawer = findViewById(R.id.settings_drawer);
        if (settingsDrawer != null) {
            Utils.setProperNavigationDrawerWidth(settingsDrawer);

            // Add the preferences to the drawer
            if (getFragmentManager().findFragmentById(R.id.settings_drawer) == null) {
                getFragmentManager().beginTransaction()
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
        String appName = "PSPDFKit";
        PackageManager packageManager = getPackageManager();
        try {
            appName = packageManager.getActivityInfo(getComponentName(), 0).loadLabel(packageManager).toString();
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
