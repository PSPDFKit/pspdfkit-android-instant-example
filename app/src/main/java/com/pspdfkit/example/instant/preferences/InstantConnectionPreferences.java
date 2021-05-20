/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import okhttp3.HttpUrl;

/**
 * Holds instant connection preferences in app's shared preferences.
 */
public class InstantConnectionPreferences {

    public static final int INSTANT_SERVER_PORT = 5000;
    public static final int WEB_EXAMPLE_SERVER_PORT = 3000;

    private static final String PREF_SERVER_URL = "instant_example.server_url";
    private static final String PREF_USERNAME = "instant_example.username";
    private static final String PREF_LOGGED_IN = "instant_example.logged_in";

    /**
     * Returns instant server url for example server URL.
     */
    @NonNull
    public static String getInstantServerUrlFromServerUrl(@NonNull String serverUrl) {
        HttpUrl serverUrlBase = HttpUrl.parse(serverUrl);
        return serverUrlBase != null ? serverUrlBase.newBuilder().port(INSTANT_SERVER_PORT).toString() : "";
    }

    /**
     * Returns instant server url.
     */
    @NonNull
    public static String getInstantServerUrl(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getInstantServerUrlFromServerUrl(sharedPref.getString(PREF_SERVER_URL, ""));
    }

    /**
     * Returns web example server url.
     */
    @NonNull
    public static String getWebExampleServerUrl(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        HttpUrl serverUrlBase = HttpUrl.parse(sharedPref.getString(PREF_SERVER_URL, ""));
        return serverUrlBase != null ? serverUrlBase.toString() : "";
    }

    /**
     * Returns configured user name from preferences.
     */
    @NonNull
    public static String getUserName(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(PREF_USERNAME, "");
    }

    /**
     * Returns {@code true} when server url and user name are already set.
     */
    public static boolean isLoggedIn(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_LOGGED_IN, false);
    }

    /**
     * Sets server url and user name to preferences.
     */
    public static void setConnectionPreferences(@NonNull Context context, @NonNull String serverUrl, @NonNull String userName) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_SERVER_URL, serverUrl);
        editor.putString(PREF_USERNAME, userName);
        editor.putBoolean(PREF_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Clears connection preferences.
     */
    public static void clearConnectionPreferences(@NonNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(PREF_SERVER_URL);
        editor.remove(PREF_USERNAME);
        editor.remove(PREF_LOGGED_IN);
        editor.apply();
    }
}
