/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.instant;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.pspdfkit.example.PSPDFKitReporting;

import io.reactivex.plugins.RxJavaPlugins;

public class PSPDFKitInstantExample extends MultiDexApplication {

    private static final String LOG_TAG = "PSPDFKitInstantExample";

    @Override
    public void onCreate() {
        super.onCreate();
        PSPDFKitReporting.initializeBugReporting(this);
        // Set error handling for unhandled RxJava exceptions.
        RxJavaPlugins.setErrorHandler(throwable -> Log.e(LOG_TAG, "Unhandled RxJava exception", throwable));
    }

}
