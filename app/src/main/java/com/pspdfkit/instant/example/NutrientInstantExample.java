/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example;

import android.app.Application;
import android.util.Log;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

public class NutrientInstantExample extends Application {

    private static final String LOG_TAG = "NutrientInstantExample";

    @Override
    public void onCreate() {
        super.onCreate();
        // Set error handling for unhandled RxJava exceptions.
        RxJavaPlugins.setErrorHandler(throwable -> Log.e(LOG_TAG, "Unhandled RxJava exception", throwable));
    }
}
