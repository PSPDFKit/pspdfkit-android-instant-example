/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.ui;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.pspdfkit.configuration.PdfConfiguration;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.example.ui.CatalogPreferencesFragment;

/**
 * This settings fragment is used to configure the {@link PdfConfiguration} used by the examples.
 */
public class InstantCatalogPreferencesFragment extends CatalogPreferencesFragment {

    @NonNull
    public static PdfActivityConfiguration.Builder getConfiguration(@NonNull Context context) {
        return CatalogPreferencesFragment.getConfiguration(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disableUnsupportedPreferences(getPreferenceScreen(), null);
    }

    private void disableUnsupportedPreferences(@NonNull PreferenceGroup preferenceGroup, @Nullable PreferenceGroup parentGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); ++i) {
            Preference pref = preferenceGroup.getPreference(i);
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
