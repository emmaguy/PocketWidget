package dev.emmaguy.pocketwidget.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
    public static final String SHARED_PREFERENCES_NAME = "pocketWidget";
    public static final String EXTRA_KEY_FROM_LAUNCHER = "from_launcher";

    public static final String POCKET_USERNAME = "username";
    public static final String POCKET_AUTH_ACCESS_TOKEN = "accesstoken";
    public static final String POCKET_AUTH_CODE = "code";

    public static final String DASHCLOCK_PACKAGE_NAME = "net.nurik.roman.dashclock";
    public static final String DASHCLOCK_CLASS_NAME_CONFIGURATION_ACTIVITY = "com.google.android.apps.dashclock.configuration.ConfigurationActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showHomeAsUp = true;

        // if we've been opened by the launcher activity, it was from a widget so don't show home as up
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean fromLauncher = extras.getBoolean(EXTRA_KEY_FROM_LAUNCHER);
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