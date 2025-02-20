/*
 *   Copyright © 2017-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import com.pspdfkit.instant.example.R;
import com.pspdfkit.instant.example.preferences.InstantConnectionPreferences;
import java.net.URL;
import okhttp3.HttpUrl;

/** Fragment for managing connection preferences screen. */
public class InstantConnectionPreferencesFragment extends Fragment {

    public static final String FRAGMENT_TAG = "InstantConnectionPreferencesFragment.FRAGMENT_TAG";
    private String serverUrl;
    private String userName;

    @Nullable
    private Callback callback;

    private TextView serverUrlView;
    private TextInputLayout serverUrlTextInputLayout;
    private TextView userNameView;
    private TextInputLayout userNameTextInputLayout;
    private Button loginButton;
    private final TextWatcher loginButtonEnabledTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
            // Do nothing
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            InstantConnectionPreferencesFragment.this.onTextChanged();
        }

        @Override
        public void afterTextChanged(final Editable editable) {
            // Do nothing
        }
    };

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getContext() != null) {
            serverUrl = InstantConnectionPreferences.getWebExampleServerUrl(getContext());
            if (TextUtils.isEmpty(serverUrl)) {
                serverUrl = getResources().getString(R.string.default_web_example_server_url);
            }
            userName = InstantConnectionPreferences.getUserName(getContext());
            if (TextUtils.isEmpty(userName)) {
                userName = getResources().getString(R.string.default_username);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_instant_connection_preferences, container, false);

        serverUrlTextInputLayout = root.findViewById(R.id.text_input_layout_server_url);
        serverUrlView = root.findViewById(R.id.input_server_url);
        userNameView = root.findViewById(R.id.input_user_name);
        userNameTextInputLayout = root.findViewById(R.id.text_input_layout_user_name);
        loginButton = root.findViewById(R.id.btn_login);

        serverUrlView.setText(serverUrl);
        userNameView.setText(userName);

        serverUrlView.addTextChangedListener(loginButtonEnabledTextWatcher);
        userNameView.addTextChangedListener(loginButtonEnabledTextWatcher);
        onTextChanged();

        loginButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                final InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        getActivity().getWindow().getDecorView().getWindowToken(), 0);
                if (callback != null) {
                    callback.onLoginRequest(serverUrl, userName);
                }
            }
        });

        return root;
    }

    private void onTextChanged() {
        serverUrl = createCanonicalServerUrl(serverUrlView.getText().toString());
        if (TextUtils.isEmpty(serverUrl)) {
            serverUrlTextInputLayout.setError(getString(R.string.error_enter_valid_server_url));
        } else {
            serverUrlTextInputLayout.setError(null);
        }

        userName = userNameView.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            userNameTextInputLayout.setError(getString(R.string.error_enter_valid_user_name));
        } else {
            userNameTextInputLayout.setError(null);
        }

        loginButton.setEnabled(!TextUtils.isEmpty(serverUrl) && !TextUtils.isEmpty(userName));
    }

    @Nullable
    private String createCanonicalServerUrl(final String serverUrl) {
        try {
            final URL url = new URL(serverUrl);
            return new HttpUrl.Builder()
                    .scheme(url.getProtocol() != null ? url.getProtocol() : "http")
                    .host(url.getHost())
                    .port(url.getPort() != -1 ? url.getPort() : InstantConnectionPreferences.WEB_EXAMPLE_SERVER_PORT)
                    .build()
                    .toString();
        } catch (final Throwable e) {
            return null;
        }
    }

    public void setCallback(@Nullable final Callback callback) {
        this.callback = callback;
    }

    /** Callback for listening to login request. */
    public interface Callback {
        void onLoginRequest(@NonNull String serverUrl, @NonNull String userName);
    }
}
