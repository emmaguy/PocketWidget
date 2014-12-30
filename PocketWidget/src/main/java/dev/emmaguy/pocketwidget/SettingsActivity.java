package dev.emmaguy.pocketwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
    public static final String FROM_LAUNCHER = "from_launcher";
    public static final String SHARED_PREFERENCES = "pocketWidget";

    public static final String POCKET_ACCOUNT = "authentication_preferencescreen";
    public static final String ACCESS_TOKEN = "accesstoken";
    public static final String USERNAME = "username";
    public static final String CODE = "code";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String REFRESH_INTERVAL = "refresh_interval";

    public static final String FORCE_REFRESH = "force_refresh";
    public static final String RATE_THIS = "rate_this";
    public static final String WIFI_ONLY_KEY = "wifi_only";
    public static final String ALWAYS_SHOW_KEY = "always_show";

    public static final String DASHCLOCK_PACKAGE_NAME = "net.nurik.roman.dashclock";
    public static final String DASHCLOCK_CLASS_NAME_CONFIGURATION_ACTIVITY = "com.google.android.apps.dashclock.configuration.ConfigurationActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showHomeAsUp = true;

        // if we've been opened by the launcher activity, it was from a widget so don't show home as up
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean fromLauncher = extras.getBoolean(FROM_LAUNCHER);
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                showHomeAsUp = false;
            }

            if (fromLauncher) {
                showHomeAsUp = false;
            }
        }

        // otherwise, likely opened by DashClock, so we want to show back arrow
        if (showHomeAsUp) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(DASHCLOCK_PACKAGE_NAME, DASHCLOCK_CLASS_NAME_CONFIGURATION_ACTIVITY));
                    NavUtils.navigateUpTo(this, intent);
                } catch (Exception ignored) {
                    // can't explicitly find/launch DashClock to go back to, so just close self
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (SettingsFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
}