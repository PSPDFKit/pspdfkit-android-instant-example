/*
 *   Copyright © 2017-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import com.pspdfkit.configuration.PdfConfiguration;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;

/**
 * This settings fragment is used to configure the {@link PdfConfiguration} used by the examples.
 */
public class InstantCatalogPreferencesFragment extends CatalogPreferencesFragment {

    @NonNull
    public static PdfActivityConfiguration.Builder getConfiguration(@NonNull final Context context) {
        return CatalogPreferencesFragment.getConfiguration(context);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disableUnsupportedPreferences(getPreferenceScreen(), null);
    }

    private void disableUnsupportedPreferences(
            @NonNull final PreferenceGroup preferenceGroup, @Nullable final PreferenceGroup parentGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); ++i) {
            final Preference pref = preferenceGroup.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                if (((PreferenceGroup) pref).getPreferenceCount() == 0) {
                    preferenceGroup.removePreference(pref);
                } else {
                    disableUnsupportedPreferences((PreferenceGroup) pref, preferenceGroup);
                }
            } else {
                if (PREF_ENABLE_FORM_EDITING.equals(pref.getKey())) {
                    preferenceGroup.removePreference(pref);
                    if (preferenceGroup.getPreferenceCount() == 0 && parentGroup != null) {
                        parentGroup.removePreference(preferenceGroup);
                    }
                }
            }
        }
    }
}
