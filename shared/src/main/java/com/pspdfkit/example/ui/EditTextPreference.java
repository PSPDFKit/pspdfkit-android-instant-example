/*
 *   Copyright © 2017-2021 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.example.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * An extension of EditTextPreference that sets the summary equals to the value it's set to.
 */
public class EditTextPreference extends android.preference.EditTextPreference {

    public EditTextPreference(Context context) {
        super(context);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return "Start the document at page: " + super.getText();
    }

}
