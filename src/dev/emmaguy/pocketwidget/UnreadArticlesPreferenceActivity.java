package dev.emmaguy.pocketwidget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import dev.emmaguy.pocketwidget.RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener;
import dev.emmaguy.pocketwidget.RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener;

public class UnreadArticlesPreferenceActivity extends PreferenceActivity implements OnUrlRetrievedListener,
        OnPreferenceClickListener, OnAccessTokenRetrievedListener, OnPreferenceChangeListener {

    public static final String SHARED_PREFERENCES = "pocketWidget";
    public static final String ACCESS_TOKEN = "accesstoken";
    public static final String USERNAME = "username";
    public static final String WIFI_ONLY = "wifionly";
    public static final String CODE = "code";
    public static final String UNREAD_COUNT = "unread_count";

    protected Method loadHeaders = null;
    protected Method hasHeaders = null;
    private int appWidgetId = -1;
    private static List<Header> headers;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     *
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
        if (hasHeaders != null && loadHeaders != null) {
            try {
                return (Boolean) hasHeaders.invoke(this);
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle aSavedState) {
        try {
            loadHeaders = ((Object) this).getClass().getMethod("loadHeadersFromResource", int.class, List.class);
            hasHeaders = ((Object) this).getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        }

        super.onCreate(aSavedState);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, 0);
        if (!isNewV11Prefs()) {
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES);

            addPreferencesFromResource(R.xml.preference_login);
            addPreferencesFromResource(R.xml.preference_refresh);
            addPreferencesFromResource(R.xml.rate_on_play_store);

            PreferenceScreen screen = (PreferenceScreen) findPreference("authentication_preferencescreen");
            if (screen != null) {
                screen.setTitle(getLoginPreferenceScreenTitle(this, prefs));
                screen.setSummary(getLoginPreferenceScreenSummary(this, prefs));
                screen.setOnPreferenceClickListener(this);
            }

            PreferenceScreen forceRefresh = (PreferenceScreen) findPreference("force_refresh");
            if (forceRefresh != null) {
                forceRefresh.setOnPreferenceClickListener(this);
            }

            PreferenceScreen rateThis = (PreferenceScreen) findPreference("rate_this");
            if (rateThis != null) {
                rateThis.setOnPreferenceClickListener(this);
            }

            ListPreference refresh = (ListPreference) findPreference("refresh_interval");
            if (refresh != null) {
                refresh.setOnPreferenceClickListener(this);
            }
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                prefs.edit().putInt("appWidgetId", appWidgetId).commit();
            } else {
                appWidgetId = prefs.getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (!prefs.getBoolean("isInitialised" + appWidgetId, false)) {
                prefs.edit().putBoolean("isInitialised" + appWidgetId, true).commit();

                final Intent intent2 = getIntent();
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, intent2);
                finish();
                startActivity(intent2);
            }
        }

        String accessToken = prefs.getString(UnreadArticlesPreferenceActivity.ACCESS_TOKEN, null);
        if (accessToken != null && accessToken.length() > 0 && appWidgetId >= 0) {
            updateAccountHeader(this, prefs);
            refreshWidget(this, appWidgetId);
            return;
        }
        updateAccountHeader(this, prefs);
    }

    @Override
    public void onBuildHeaders(List<Header> headersList) {
        try {
            headers = headersList;
            loadHeaders.invoke(this, new Object[]{R.xml.preference_headers, headersList});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static public class PrefsFragment extends PreferenceFragment implements OnPreferenceClickListener,
            OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES);
            SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);

            Context context = getActivity().getApplicationContext();
            Resources resources = context.getResources();
            String resourcesValue = getArguments().getString("pref-resource");

            int thePrefRes = resources.getIdentifier(resourcesValue, "xml", context.getPackageName());
            addPreferencesFromResource(thePrefRes);

            PreferenceScreen screen = (PreferenceScreen) findPreference("authentication_preferencescreen");
            if (screen != null) {
                screen.setTitle(getLoginPreferenceScreenTitle(getActivity(), prefs));
                screen.setSummary(getLoginPreferenceScreenSummary(getActivity(), prefs));
                screen.setOnPreferenceClickListener(this);
            }

            PreferenceScreen forceRefresh = (PreferenceScreen) findPreference("force_refresh");
            if (forceRefresh != null) {
                forceRefresh.setOnPreferenceClickListener(this);
            }

            PreferenceScreen rateThis = (PreferenceScreen) findPreference("rate_this");
            if (rateThis != null) {
                rateThis.setOnPreferenceClickListener(this);
            }

            ListPreference refresh = (ListPreference) findPreference("refresh_interval");
            if (refresh != null) {
                refresh.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            retrieveAccessToken(getActivity());
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return onPrefsClick(preference, getActivity());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return onPrefsChange(preference, getActivity(), newValue.toString());
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return onPrefsClick(preference, this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return onPrefsChange(preference, this, newValue.toString());
    }

    private static boolean onPrefsChange(Preference preference, Activity a, String newValue) {
        if (preference.getKey().equals("refresh_interval")) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(a);
            ComponentName thisWidget = new ComponentName(a, UnreadArticlesWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            if (allWidgetIds != null && allWidgetIds.length > 0) {
                UnreadArticlesWidgetProvider.createOrUpdateService(a, newValue);
            }
            return true;
        }

        return false;
    }

    private static boolean onPrefsClick(Preference preference, Activity a) {
        final SharedPreferences sharedPreferences = a.getSharedPreferences(SHARED_PREFERENCES, 0);

        if (preference.getKey().equals("authentication_preferencescreen")) {
            beginSignInProcessOrSignOut(a);
            updateAccountHeader(a, sharedPreferences);
            return true;
        } else if (preference.getKey().equals("force_refresh")) {
            int appWidgetId = sharedPreferences.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            refreshWidget(a, appWidgetId);
            Toast.makeText(a, a.getString(R.string.refreshing), Toast.LENGTH_LONG).show();
            return true;
        } else if (preference.getKey().equals("rate_this")) {
            try {
                Uri uri = Uri.parse("market://details?id=dev.emmaguy.pocketwidget");
                a.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception e) {
                Log.e("PocketCountWidget", a.getString(R.string.failed_to_launch_market));
                Toast.makeText(a, a.getString(R.string.failed_to_launch_market), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    @Override
    public void onRetrievedUrl(String url) {
        redirectToBrowser(url, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        retrieveAccessToken(this);
    }

    @Override
    public void onRetrievedAccessToken() {
        updateAccountHeader(this, getSharedPreferences(SHARED_PREFERENCES, 0));
        showHomeScreenAndFinish(this);
    }

    private static void updateAccountHeader(Activity a, SharedPreferences p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            buildHeader(a, p);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void buildHeader(Activity a, SharedPreferences p) {
        if (headers != null && headers.size() > 0) {
            Header account = headers.get(0);
            final String username = p.getString(USERNAME, null);
            if (username != null && username.length() > 0) {
                account.title = a.getString(R.string.pocket_account) + " (" + username + ")";
            } else {
                account.title = a.getString(R.string.pocket_account);
            }
        }
    }

    private static void retrieveAccessToken(Activity activity) {
        Uri uri = activity.getIntent().getData();
        if (uri != null && uri.toString().startsWith("pocketwidget")
                && !isSignedIn(activity.getSharedPreferences(SHARED_PREFERENCES, 0))) {
            new RetrieveAccessTokenAsyncTask(activity.getResources().getString(R.string.pocket_consumer_key_mobile),
                    (OnAccessTokenRetrievedListener) activity, activity.getSharedPreferences(SHARED_PREFERENCES, 0),
                    activity, activity.getString(R.string.retrieving_access_token)).execute();
        }
    }

    private static void redirectToBrowser(String url, final Activity activity) {
        Toast.makeText(activity, activity.getString(R.string.redirecting_to_browser), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.finish();
        activity.startActivity(intent);
    }

    private static void beginSignInProcessOrSignOut(final Activity activity) {
        if (isSignedIn(activity.getSharedPreferences(SHARED_PREFERENCES, 0))) {
            activity.getSharedPreferences(SHARED_PREFERENCES, 0).edit().remove(CODE).remove(ACCESS_TOKEN)
                    .remove(USERNAME).remove(UNREAD_COUNT).commit();
            Toast.makeText(activity, activity.getString(R.string.account_cleared), Toast.LENGTH_LONG).show();

            UnreadArticlesWidgetProvider.killService(activity);

            // refresh this activity
            activity.finish();
            activity.startActivity(activity.getIntent());
        } else {
            new RetrieveRequestTokenAsyncTask(activity.getResources().getString(R.string.pocket_consumer_key_mobile),
                    (OnUrlRetrievedListener) activity, activity.getSharedPreferences(SHARED_PREFERENCES, 0), activity,
                    activity.getString(R.string.retrieving_request_token)).execute();
        }
    }

    private static String getLoginPreferenceScreenTitle(Activity a, SharedPreferences p) {
        if (isSignedIn(p)) {
            final String username = p.getString(USERNAME, null);
            return a.getString(R.string.sign_out) + username;
        }

        return a.getString(R.string.sign_in);
    }

    private static String getLoginPreferenceScreenSummary(Activity a, SharedPreferences p) {
        if (isSignedIn(p)) {
            return a.getString(R.string.tap_to_sign_out);
        }

        return a.getString(R.string.tap_to_sign_in);
    }

    private static boolean isSignedIn(SharedPreferences p) {
        String accessToken = p.getString(ACCESS_TOKEN, null);
        return accessToken != null && accessToken.length() > 0;
    }

    private static void showHomeScreenAndFinish(final Activity activity) {
        final SharedPreferences preferences = activity.getSharedPreferences(SHARED_PREFERENCES, 0);
        Toast.makeText(activity, activity.getString(R.string.successfully_logged_in_as) + preferences.getString(USERNAME, null),
                Toast.LENGTH_LONG).show();

        int appWidgetId = preferences
                .getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            refreshWidget(activity, appWidgetId);
        } else {
            activity.finish();
            activity.startActivity(activity.getIntent());
        }
    }

    private static void refreshWidget(final Activity activity, int appWidgetId) {
        new UnreadArticlesWidgetProvider().onUpdate(activity, AppWidgetManager.getInstance(activity),
                new int[]{appWidgetId});
    }
}