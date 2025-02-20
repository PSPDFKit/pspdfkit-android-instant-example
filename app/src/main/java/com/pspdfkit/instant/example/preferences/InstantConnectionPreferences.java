/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import okhttp3.HttpUrl;

/** Holds instant connection preferences in app's shared preferences. */
public class InstantConnectionPreferences {

    public static final int INSTANT_SERVER_PORT = 5000;
    public static final int WEB_EXAMPLE_SERVER_PORT = 3000;

    private static final String PREF_SERVER_URL = "instant_example.server_url";
    private static final String PREF_USERNAME = "instant_example.username";
    private static final String PREF_LOGGED_IN = "instant_example.logged_in";

    /** Returns Instant Server (Nutrient Document Engine) url for example server URL. */
    @NonNull
    public static String getInstantServerUrlFromServerUrl(@NonNull final String serverUrl) {
        final HttpUrl serverUrlBase = HttpUrl.parse(serverUrl);
        return serverUrlBase != null
                ? serverUrlBase.newBuilder().port(INSTANT_SERVER_PORT).toString()
                : "";
    }

    /** Returns Instant Server (Nutrient Document Engine) url. */
    @NonNull
    public static String getInstantServerUrl(@NonNull final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return getInstantServerUrlFromServerUrl(sharedPref.getString(PREF_SERVER_URL, ""));
    }

    /** Returns web example server url. */
    @NonNull
    public static String getWebExampleServerUrl(@NonNull final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        final HttpUrl serverUrlBase = HttpUrl.parse(sharedPref.getString(PREF_SERVER_URL, ""));
        return serverUrlBase != null ? serverUrlBase.toString() : "";
    }

    /** Returns configured user name from preferences. */
    @NonNull
    public static String getUserName(@NonNull final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(PREF_USERNAME, "");
    }

    /** Returns {@code true} when server url and user name are already set. */
    public static boolean isLoggedIn(@NonNull final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_LOGGED_IN, false);
    }

    /** Sets server url and user name to preferences. */
    public static void setConnectionPreferences(
            @NonNull final Context context, @NonNull final String serverUrl, @NonNull final String userName) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREF_SERVER_URL, serverUrl);
        editor.putString(PREF_USERNAME, userName);
        editor.putBoolean(PREF_LOGGED_IN, true);
        editor.apply();
    }

    /** Clears connection preferences. */
    public static void clearConnectionPreferences(@NonNull final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(PREF_SERVER_URL);
        editor.remove(PREF_USERNAME);
        editor.remove(PREF_LOGGED_IN);
        editor.apply();
    }
}
