package dev.emmaguy.pocketwidget;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import timber.log.Timber;

public class AnalyticsTracker {

    public static final String LOG_EVENT_FORCE_REFRESH = "ForceRefresh";
    public static final String LOG_EVENT_VIEW_GRAPH = "ViewGraph";

    public static final String LOG_CATEGORY = "PocketWidget";
    public static final String LOG_OPEN_POCKET_APP = "OpenPocketApp";
    public static final String LOG_DASHCLOCK_EVENT = "DashClock";
    public static final String LOG_WIDGET_EVENT = "Widget";

    public static final String LOG_SIGN_IN_START = "SignInStart";
    public static final String LOG_SIGN_IN_REDIRECT_TO_BROWSER = "SignInRedirectToBrowser";
    public static final String LOG_SIGN_IN_RETURN_FROM_BROWSER = "SignInRedirectBrowser";
    public static final String LOG_SIGN_IN_NO_TOKEN = "SignInNoToken";
    public static final String LOG_SIGN_IN_SUCCESS = "SignInSuccess";

    private FirebaseAnalytics firebaseAnalytics;

    public AnalyticsTracker(@NonNull final Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void sendEvent(@NonNull final String category, @NonNull final String action) {
        if (BuildConfig.DEBUG) {
            Timber.d("Sending event, category: %s action: %s", category, action);
        }

        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, action);

        firebaseAnalytics.logEvent(category, bundle);
    }
}
