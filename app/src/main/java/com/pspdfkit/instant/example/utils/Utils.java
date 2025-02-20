/*
 *   Copyright © 2017-2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import com.pspdfkit.instant.example.R;

// We can't use AndroidVersion.INSTANCE.isAtLeastMarshmallow in this
// class because  of the way it gets obfuscated after building
public class Utils {
    /**
     * Licensed under the Apache License, Version 2.0, from <a
     * href="https://github.com/consp1racy/material-navigation-drawer/blob/master/navigation-drawer/src/main/java/net/xpece/material/navigationdrawer/NavigationDrawerUtils.java">source</a>
     */
    public static void setProperNavigationDrawerWidth(final View view) {
        final Context context = view.getContext();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int smallestWidthPx = Math.min(
                        context.getResources().getDisplayMetrics().widthPixels,
                        context.getResources().getDisplayMetrics().heightPixels);
                int drawerMargin = context.getResources().getDimensionPixelOffset(R.dimen.drawer_margin);

                view.getLayoutParams().width = Math.min(
                        context.getResources().getDimensionPixelSize(R.dimen.drawer_max_width),
                        smallestWidthPx - drawerMargin);
                view.requestLayout();
            }
        });
    }
}
