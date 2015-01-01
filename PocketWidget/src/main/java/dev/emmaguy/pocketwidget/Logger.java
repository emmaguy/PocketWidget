package dev.emmaguy.pocketwidget;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

public class Logger {
    public static final String LOG_EVENT_FORCE_REFRESH = "ForceRefresh";
    public static final String LOG_EVENT_VIEW_GRAPH = "ViewGraph";

    public static final String LOG_CATEGORY = "PocketWidget";
    public static final String TAG_POCKET_WIDGET = "PocketWidget";
    public static final String LOG_OPEN_POCKET_APP = "OpenPocketApp";
    public static final String LOG_DASHCLOCK_EVENT = "DashClock";
    public static final String LOG_WIDGET_EVENT = "Widget";

    public static final String LOG_SIGN_IN_START = "SignInStart";
    public static final String LOG_SIGN_IN_REDIRECT_TO_BROWSER = "SignInRedirectToBrowser";
    public static final String LOG_SIGN_IN_RETURN_FROM_BROWSER = "SignInRedirectBrowser";
    public static final String LOG_SIGN_IN_NO_TOKEN = "SignInNoToken";
    public static final String LOG_SIGN_IN_SUCCESS = "SignInSuccess";

    private static Tracker mTracker;
    private static boolean sIsDebug = BuildConfig.DEBUG;

    public static void Log(String message) {
        if (sIsDebug) {
            Log.d(TAG_POCKET_WIDGET, message);
        }
    }

    public static void sendThrowable(Context c, String message, Throwable t) {
        if (sIsDebug) {
            Log.e(TAG_POCKET_WIDGET, message, t);
        } else {
            String description = message + " " + t.getMessage() + " " + new StandardExceptionParser(c, null).getDescription(Thread.currentThread().getName(), t);
            getTracker(c)
                    .send(new HitBuilders.ExceptionBuilder()
                            .setDescription(description)
                            .setFatal(false)
                            .build());
        }
    }

    public static void sendEvent(Context c, String action) {
        if (sIsDebug) {
            Log("Sending event: " + action);
        } else {
            getTracker(c)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(LOG_CATEGORY)
                            .setAction(action)
                            .build());
        }
    }

    private static synchronized Tracker getTracker(Context c) {
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(c).newTracker(R.xml.google_analytics);
            if (sIsDebug) {
                GoogleAnalytics.getInstance(c).getLogger().setLogLevel(com.google.android.gms.analytics.Logger.LogLevel.VERBOSE);
            }
        }
        return mTracker;
    }
}
