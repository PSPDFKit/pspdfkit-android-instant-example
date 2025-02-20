/*
 *   Copyright © 2017-2024 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.configuration.PdfConfiguration;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.activity.ThumbnailBarMode;
import com.pspdfkit.configuration.activity.UserInterfaceViewMode;
import com.pspdfkit.configuration.annotations.AnnotationReplyFeatures;
import com.pspdfkit.configuration.page.PageFitMode;
import com.pspdfkit.configuration.page.PageLayoutMode;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.configuration.sharing.ShareFeatures;
import com.pspdfkit.configuration.theming.ThemeMode;
import com.pspdfkit.instant.example.BuildConfig;
import com.pspdfkit.instant.example.R;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Objects;

/**
 * This settings fragment is used to configure the {@link PdfConfiguration} used by the examples.
 */
public class CatalogPreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String PREF_PAGE_SCROLL_DIRECTION = "page_scroll_direction";
    protected static final String PREF_PAGE_LAYOUT_MODE = "page_layout_mode";
    protected static final String PREF_PAGE_SCROLL_CONTINUOUS = "scroll_continuously";
    protected static final String PREF_FIT_PAGE_TO_WIDTH = "fit_page_to_width";
    protected static final String PREF_FIRST_PAGE_AS_SINGLE = "first_page_as_single";
    protected static final String PREF_SHOW_GAP_BETWEEN_PAGES = "show_gap_between_pages";
    protected static final String PREF_IMMERSIVE_MODE = "immersive_mode";
    protected static final String PREF_SYSTEM_USER_INTERFACE_MODE = "user_interface_view_mode";
    protected static final String PREF_HIDE_UI_WHEN_CREATING_ANNOTATIONS = "hide_ui_when_creating_annotations";
    protected static final String PREF_SHOW_SEARCH_ACTION = "show_search_action";
    protected static final String PREF_INLINE_SEARCH = "inline_search";
    protected static final String PREF_THUMBNAIL_BAR_MODE = "thumbnail_bar_mode";
    protected static final String PREF_SHOW_THUMBNAIL_GRID_ACTION = "show_thumbnail_grid_action";
    protected static final String PREF_SHOW_OUTLINE_ACTION = "show_outline_action";
    protected static final String PREF_SHOW_ANNOTATION_LIST_ACTION = "show_annotation_list_action";
    protected static final String PREF_SHOW_PAGE_NUMBER_OVERLAY = "show_page_number_overlay";
    protected static final String PREF_SHOW_PAGE_LABELS = "show_page_labels";
    protected static final String PREF_INVERT_COLORS = "invert_colors";
    protected static final String PREF_GRAYSCALE = "grayscale";
    protected static final String PREF_START_PAGE = "start_page";
    protected static final String PREF_RESTORE_LAST_VIEWED_PAGE = "restore_last_viewed_page";
    protected static final String PREF_CLEAR_CACHE = "clear_cache";
    protected static final String PREF_CLEAR_APP_DATA = "clear_app_data";
    protected static final String PREF_ENABLE_ANNOTATION_EDITING = "enable_annotation_editing";
    protected static final String PREF_ENABLE_ANNOTATION_ROTATION = "enable_annotation_rotation";
    protected static final String PREF_ENABLE_ANNOTATION_REPLIES = "annotation_reply_features";
    protected static final String PREF_ENABLE_TEXT_SELECTION = "enable_text_selection";
    protected static final String PREF_ENABLE_FORM_EDITING = "enable_form_editing";
    protected static final String PREF_SHOW_SHARE_ACTION = "show_share_action";
    protected static final String PREF_SHOW_PRINT_ACTION = "show_print_action";
    protected static final String PREF_THEME_MODE = "theme_mode";
    protected static final String PREF_ENABLE_VOLUME_BUTTONS_NAVIGATION = "enable_volume_buttons_navigation";
    protected static final String PREF_MULTI_THREADED_RENDERING = "multi_threaded_rendering";

    public static final String PREF_PREFERRED_LANGUAGE = "preferred_language";
    public static final String PREF_LEAK_CANARY_ENABLED = "leak_canary_enabled";

    /** Initializes default values in preferences. */
    public static void initializeDefaultValues(@NonNull final Context context) {
        Completable.fromAction(() -> PreferenceManager.setDefaultValues(context, R.xml.preferences, false))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /** Returns configuration from values set in shared preferences. */
    @NonNull
    public static PdfActivityConfiguration.Builder getConfiguration(@NonNull final Context context) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        final String pageScrollDirectionHorizontal = context.getString(R.string.page_scroll_direction_horizontal);
        final String userInterfaceViewModeDefault = context.getString(R.string.user_interface_view_mode_automatic);
        final String thumbnailModeDefault = context.getString(R.string.thumbnail_bar_mode_floating);
        final String annotationReplyDefault = context.getString(R.string.annotation_reply_enabled);

        final PageScrollDirection pageScrollDirection =
                isStringValueSet(sharedPref, PREF_PAGE_SCROLL_DIRECTION, pageScrollDirectionHorizontal)
                        ? PageScrollDirection.HORIZONTAL
                        : PageScrollDirection.VERTICAL;

        final PageScrollMode pageScrollMode = getBooleanValue(sharedPref, PREF_PAGE_SCROLL_CONTINUOUS)
                ? PageScrollMode.CONTINUOUS
                : PageScrollMode.PER_PAGE;

        final PageFitMode pageFitMode = getBooleanValue(sharedPref, PREF_FIT_PAGE_TO_WIDTH)
                ? PageFitMode.FIT_TO_WIDTH
                : PageFitMode.FIT_TO_SCREEN;

        final PageLayoutMode pageLayoutMode;
        if (isStringValueSet(sharedPref, PREF_PAGE_LAYOUT_MODE, context.getString(R.string.page_layout_single))) {
            pageLayoutMode = PageLayoutMode.SINGLE;
        } else if (isStringValueSet(
                sharedPref, PREF_PAGE_LAYOUT_MODE, context.getString(R.string.page_layout_double))) {
            pageLayoutMode = PageLayoutMode.DOUBLE;
        } else {
            pageLayoutMode = PageLayoutMode.AUTO;
        }

        final ThemeMode themeMode;
        if (isStringValueSet(sharedPref, PREF_THEME_MODE, context.getString(R.string.theme_mode_night))) {
            themeMode = ThemeMode.NIGHT;
        } else {
            themeMode = ThemeMode.DEFAULT;
        }

        final boolean restoreLastViewedPage = getBooleanValue(sharedPref, PREF_RESTORE_LAST_VIEWED_PAGE);
        final int searchType = getBooleanValue(sharedPref, PREF_INLINE_SEARCH)
                ? PdfActivityConfiguration.SEARCH_INLINE
                : PdfActivityConfiguration.SEARCH_MODULAR;

        final UserInterfaceViewMode userInterfaceViewMode = getUserInterfaceModeFromPreferenceString(
                context, getStringValue(sharedPref, PREF_SYSTEM_USER_INTERFACE_MODE, userInterfaceViewModeDefault));

        final ThumbnailBarMode thumbnailBarMode = getThumbnailBarModeFromPreferenceString(
                context, getStringValue(sharedPref, PREF_THUMBNAIL_BAR_MODE, thumbnailModeDefault));

        final AnnotationReplyFeatures annotationReplyFeatures = getAnnotationReplyFeaturesFromPreferenceString(
                context, getStringValue(sharedPref, PREF_ENABLE_ANNOTATION_REPLIES, annotationReplyDefault));

        final boolean hideUserInterfaceWhenCreatingAnnotations =
                getBooleanValue(sharedPref, PREF_HIDE_UI_WHEN_CREATING_ANNOTATIONS);

        final boolean firstPageAlwaysSingle = getBooleanValue(sharedPref, PREF_FIRST_PAGE_AS_SINGLE);
        final boolean showGapBetweenPages = getBooleanValue(sharedPref, PREF_SHOW_GAP_BETWEEN_PAGES);

        int startPage;
        try {
            startPage = Integer.parseInt(getStringValue(sharedPref, PREF_START_PAGE, "0"));
        } catch (final NumberFormatException ex) {
            startPage = 0;
            sharedPref.edit().putString(PREF_START_PAGE, "0").apply();
        }

        final PdfActivityConfiguration.Builder configuration = new PdfActivityConfiguration.Builder(context)
                .scrollDirection(pageScrollDirection)
                .scrollMode(pageScrollMode)
                .fitMode(pageFitMode)
                .layoutMode(pageLayoutMode)
                .themeMode(themeMode)
                .firstPageAlwaysSingle(firstPageAlwaysSingle)
                .showGapBetweenPages(showGapBetweenPages)
                .restoreLastViewedPage(restoreLastViewedPage)
                .setUserInterfaceViewMode(userInterfaceViewMode)
                .hideUserInterfaceWhenCreatingAnnotations(hideUserInterfaceWhenCreatingAnnotations)
                .setSearchType(searchType)
                .setThumbnailBarMode(thumbnailBarMode)
                .annotationReplyFeatures(annotationReplyFeatures);

        if (startPage != 0) {
            configuration.page(startPage);
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_SEARCH_ACTION)) {
            configuration.enableSearch();
        } else {
            configuration.disableSearch();
        }

        configuration.useImmersiveMode(getBooleanValue(sharedPref, PREF_IMMERSIVE_MODE));

        if (getBooleanValue(sharedPref, PREF_SHOW_THUMBNAIL_GRID_ACTION)) {
            configuration.showThumbnailGrid();
        } else {
            configuration.hideThumbnailGrid();
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_OUTLINE_ACTION)) {
            configuration.enableOutline();
        } else {
            configuration.disableOutline();
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_ANNOTATION_LIST_ACTION)) {
            configuration.enableAnnotationList();
        } else {
            configuration.disableAnnotationList();
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_PAGE_NUMBER_OVERLAY)) {
            configuration.showPageNumberOverlay();
        } else {
            configuration.hidePageNumberOverlay();
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_PAGE_LABELS)) {
            configuration.showPageLabels();
        } else {
            configuration.hidePageLabels();
        }

        configuration.toGrayscale(getBooleanValue(sharedPref, PREF_GRAYSCALE));

        configuration.invertColors(getBooleanValue(sharedPref, PREF_INVERT_COLORS) || themeMode == ThemeMode.NIGHT);

        if (getBooleanValue(sharedPref, PREF_ENABLE_ANNOTATION_EDITING)) {
            configuration.enableAnnotationEditing();
        } else {
            configuration.disableAnnotationEditing();
        }

        if (getBooleanValue(sharedPref, PREF_ENABLE_ANNOTATION_ROTATION)) {
            configuration.enableAnnotationRotation();
        } else {
            configuration.disableAnnotationRotation();
        }

        if (getBooleanValue(sharedPref, PREF_ENABLE_FORM_EDITING)) {
            configuration.enableFormEditing();
        } else {
            configuration.disableFormEditing();
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_SHARE_ACTION)) {
            configuration.setEnabledShareFeatures(ShareFeatures.all());
        } else {
            configuration.setEnabledShareFeatures(ShareFeatures.none());
        }

        if (getBooleanValue(sharedPref, PREF_SHOW_PRINT_ACTION)) {
            configuration.enablePrinting();
        } else {
            configuration.disablePrinting();
        }

        configuration.textSelectionEnabled(getBooleanValue(sharedPref, PREF_ENABLE_TEXT_SELECTION));

        configuration.setVolumeButtonsNavigationEnabled(
                getBooleanValue(sharedPref, PREF_ENABLE_VOLUME_BUTTONS_NAVIGATION));

        configuration.setMultithreadedRenderingEnabled(getBooleanValue(sharedPref, PREF_MULTI_THREADED_RENDERING));

        return configuration;
    }

    Preference clearCacheBtn;
    Preference clearAppDataBtn;

    private static ThumbnailBarMode getThumbnailBarModeFromPreferenceString(
            @NonNull final Context context, @NonNull final String thumbnailBarModePreferenceValue) {
        ThumbnailBarMode thumbnailBarMode = ThumbnailBarMode.THUMBNAIL_BAR_MODE_PINNED;
        if (thumbnailBarModePreferenceValue.equals(context.getString(R.string.thumbnail_bar_mode_scrollable))) {
            thumbnailBarMode = ThumbnailBarMode.THUMBNAIL_BAR_MODE_SCROLLABLE;
        } else if (thumbnailBarModePreferenceValue.equals(context.getString(R.string.thumbnail_bar_mode_none))) {
            thumbnailBarMode = ThumbnailBarMode.THUMBNAIL_BAR_MODE_NONE;
        } else if (thumbnailBarModePreferenceValue.equals(context.getString(R.string.thumbnail_bar_mode_floating))) {
            thumbnailBarMode = ThumbnailBarMode.THUMBNAIL_BAR_MODE_FLOATING;
        }
        return thumbnailBarMode;
    }

    private static AnnotationReplyFeatures getAnnotationReplyFeaturesFromPreferenceString(
            @NonNull final Context context, @NonNull final String annotationReplyModeValue) {
        AnnotationReplyFeatures features = AnnotationReplyFeatures.ENABLED;
        if (annotationReplyModeValue.equalsIgnoreCase(context.getString(R.string.annotation_reply_read_only))) {
            features = AnnotationReplyFeatures.READ_ONLY;
        } else if (annotationReplyModeValue.equalsIgnoreCase(context.getString(R.string.annotation_reply_disabled))) {
            features = AnnotationReplyFeatures.DISABLED;
        }
        return features;
    }

    private static UserInterfaceViewMode getUserInterfaceModeFromPreferenceString(
            @NonNull final Context context, @NonNull final String userInterfaceModePreferenceValue) {
        UserInterfaceViewMode userInterfaceMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_AUTOMATIC;

        if (userInterfaceModePreferenceValue.equals(
                context.getString(R.string.user_interface_view_mode_automatic_border_pages))) {
            userInterfaceMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_AUTOMATIC_BORDER_PAGES;
        } else if (userInterfaceModePreferenceValue.equals(
                context.getString(R.string.user_interface_view_mode_visible))) {
            userInterfaceMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_VISIBLE;
        } else if (userInterfaceModePreferenceValue.equals(
                context.getString(R.string.user_interface_view_mode_hidden))) {
            userInterfaceMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_HIDDEN;
        } else if (userInterfaceModePreferenceValue.equals(
                context.getString(R.string.user_interface_view_mode_manual))) {
            userInterfaceMode = UserInterfaceViewMode.USER_INTERFACE_VIEW_MODE_MANUAL;
        }
        return userInterfaceMode;
    }

    private static boolean isStringValueSet(
            final SharedPreferences sharedPref, final String key, final String expected) {
        return sharedPref.getString(key, "").equals(expected);
    }

    private static boolean getBooleanValue(final SharedPreferences sharedPref, final String key) {
        return sharedPref.getBoolean(key, false);
    }

    private static String getStringValue(
            final SharedPreferences sharedPref, final String key, final String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

    @Override
    public void onCreatePreferences(@Nullable final Bundle savedInstanceState, @Nullable final String rootKey) {
        final StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            addPreferencesFromResource(R.xml.preferences);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        clearCacheBtn = findPreference(PREF_CLEAR_CACHE);
        if (clearCacheBtn != null) {
            clearCacheBtn.setOnPreferenceClickListener(preference -> {
                PSPDFKit.clearCaches(requireActivity(), true);
                Toast.makeText(getActivity(), "Cache cleared.", Toast.LENGTH_SHORT)
                        .show();
                return true;
            });
        }

        clearAppDataBtn = findPreference(PREF_CLEAR_APP_DATA);
        if (clearAppDataBtn != null) {
            clearAppDataBtn.setOnPreferenceClickListener(preference -> {
                ((ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE))
                        .clearApplicationUserData();
                return true;
            });
        }

        try {
            Class.forName("leakcanary.LeakCanary");
            if (!BuildConfig.DEBUG) {
                // Disable LeakCanary preference in release builds.
                disablePreference(
                        getPreferenceScreen(), Objects.requireNonNull(findPreference(PREF_LEAK_CANARY_ENABLED)));
            }
        } catch (final ClassNotFoundException e) {
            // Disable LeakCanary preference when library is not available.
            disablePreference(getPreferenceScreen(), Objects.requireNonNull(findPreference(PREF_LEAK_CANARY_ENABLED)));
        }

        try {
            Class.forName("com.pspdfkit.catalog.PSPDFExample");
        } catch (final ClassNotFoundException e) {
            // Disable preferred language preference if example project does not have list of
            // examples (i.e. this is an Instant example).
            disablePreference(getPreferenceScreen(), Objects.requireNonNull(findPreference(PREF_PREFERRED_LANGUAGE)));
        }

        // Ensure proper defaults are set.
        initializeDefaultValues(requireActivity());
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @NonNull
    @Override
    public View onCreateView(
            @SuppressWarnings("NullableProblems") final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        // Keep the same padding on all devices.
        final ListView lv = view.findViewById(android.R.id.list);
        if (lv != null) lv.setPadding(10, 10, 10, 10);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (PREF_ENABLE_FORM_EDITING.equals(key)) {
            PSPDFKit.clearCaches(requireActivity(), true);
        }
    }

    private void disablePreference(
            @NonNull final PreferenceGroup preferenceGroup, @NonNull final Preference preferenceToRemove) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); ++i) {
            final Preference preference = preferenceGroup.getPreference(i);
            if (preference == preferenceToRemove) {
                preferenceGroup.removePreference(preference);
                return;
            } else if (preference instanceof PreferenceGroup) {
                disablePreference((PreferenceGroup) preference, preferenceToRemove);
            }
        }
    }
}
