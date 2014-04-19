package dev.emmaguy.pocketwidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
    public static final String FROM_LAUNCHER = "from_launcher";
    public static final String SHARED_PREFERENCES = "pocketWidget";

    public static final String POCKET_ACCOUNT = "authentication_preferencescreen";
    public static final String ACCESS_TOKEN = "accesstoken";
    public static final String USERNAME = "username";
    public static final String WIFI_ONLY = "wifionly";
    public static final String CODE = "code";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String REFRESH_INTERVAL = "refresh_interval";

    private static final String FORCE_REFRESH = "force_refresh";
    private static final String RATE_THIS = "rate_this";
    private static final String WIFI_ONLY_KEY = "wifi_only";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showHomeAsUp = true;

        // if we've been opened by the launcher activity, it was from a widget so don't show home as up
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            boolean fromLauncher = extras.getBoolean(FROM_LAUNCHER);
            if(fromLauncher) {
                showHomeAsUp = false;
            }
        }

        // otherwise, likely opened by DashClock, so we want to show back arrow
        if(showHomeAsUp) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:

                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("net.nurik.roman.dashclock", "com.google.android.apps.dashclock.configuration.ConfigurationActivity"));
                    NavUtils.navigateUpTo(this, intent);
                } catch (Exception ignored) {
                    // can't explicitly find/launch DashClock somehow to go back to, so just close self
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener, RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener {
        private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(SHARED_PREFERENCES);

            addPreferencesFromResource(R.xml.preferences);

            retrieveAccessToken();
            parseWidgetExtras();
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            initSummary();

            PreferenceScreen screen = (PreferenceScreen) findPreference(POCKET_ACCOUNT);
            if (screen != null) {
                screen.setOnPreferenceClickListener(this);
            }

            PreferenceScreen forceRefresh = (PreferenceScreen) findPreference(FORCE_REFRESH);
            if (forceRefresh != null) {
                forceRefresh.setOnPreferenceClickListener(this);
            }

            PreferenceScreen rateThis = (PreferenceScreen) findPreference(RATE_THIS);
            if (rateThis != null) {
                rateThis.setOnPreferenceClickListener(this);
            }

            ListPreference refresh = (ListPreference) findPreference(REFRESH_INTERVAL);
            if (refresh != null) {
                refresh.setOnPreferenceClickListener(this);
            }
        }

        private void parseWidgetExtras() {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    getPreferenceManager().getSharedPreferences().edit().putInt("appWidgetId", appWidgetId).apply();
                } else {
                    appWidgetId = getPreferenceManager().getSharedPreferences().getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                if (!getPreferenceManager().getSharedPreferences().getBoolean("isInitialised" + appWidgetId, false)) {
                    getPreferenceManager().getSharedPreferences().edit().putBoolean("isInitialised" + appWidgetId, true).apply();

                    final Intent intent2 = getIntent();
                    intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, intent2);
                    finish();
                    startActivity(intent2);
                }
            }

            String accessToken = getPreferenceManager().getSharedPreferences().getString(ACCESS_TOKEN, null);
            if (accessToken != null && accessToken.length() > 0 && appWidgetId >= 0) {
                refreshWidget(appWidgetId);
                return;
            }
        }

        private void retrieveAccessToken() {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith("pocketwidget") && !isSignedIn()) {
                String code = getPreferenceManager().getSharedPreferences().getString(CODE, null);

                new RetrieveAccessTokenAsyncTask(getString(R.string.pocket_consumer_key_mobile),
                        this, getActivity(), getString(R.string.retrieving_access_token), code).execute();
            }
        }

        protected void initSummary() {
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initPrefsSummary(getPreferenceManager().getSharedPreferences(), getPreferenceScreen().getPreference(i));
            }
        }

        protected void initPrefsSummary(SharedPreferences sharedPreferences, Preference p) {
            if (p instanceof PreferenceCategory) {
                PreferenceCategory pCat = (PreferenceCategory) p;
                int pcCatCount = pCat.getPreferenceCount();
                for (int i = 0; i < pcCatCount; i++) {
                    initPrefsSummary(sharedPreferences, pCat.getPreference(i));
                }
            } else {
                updatePrefsSummary(sharedPreferences, p);
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePrefsSummary(sharedPreferences, findPreference(key));

            if (key.equals(REFRESH_INTERVAL)) {
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                ComponentName thisWidget = new ComponentName(getActivity(), UnreadArticlesWidgetProvider.class);
                int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                if (allWidgetIds != null && allWidgetIds.length > 0) {
                    UnreadArticlesWidgetProvider.createOrUpdateService(getApplicationContext(), getPreferenceManager().getSharedPreferences().getString(key, null));
                }
            }
        }

        protected void updatePrefsSummary(SharedPreferences sharedPreferences, Preference pref) {
            if (pref == null)
                return;

            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                listPref.setSummary(listPref.getEntry());
            } else if (pref instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference)pref;

                if(checkBoxPreference.hasKey() && checkBoxPreference.getKey().equals(WIFI_ONLY_KEY)) {
                    if(checkBoxPreference.isChecked()) {
                        checkBoxPreference.setSummary(R.string.auto_sync_summary_wifi_only);
                    } else {
                        checkBoxPreference.setSummary(R.string.auto_sync_summary_wifi_and_mobile);
                    }
                }
            } else if(pref instanceof PreferenceScreen) {
                PreferenceScreen screen = (PreferenceScreen)pref;

                if(screen.hasKey() && screen.getKey().equals(POCKET_ACCOUNT)) {
                    if(isSignedIn()) {
                        screen.setSummary(getString(R.string.sign_out) + " " + sharedPreferences.getString(USERNAME, null));
                    } else {
                        screen.setSummary(R.string.tap_to_sign_in);
                    }
                }
            }
        }

        private boolean isSignedIn() {
            String accessToken = getPreferenceScreen().getSharedPreferences().getString(ACCESS_TOKEN, null);
            return accessToken != null && accessToken.length() > 0;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getKey().equals(POCKET_ACCOUNT)) {
                beginSignInProcessOrSignOut();
                updatePrefsSummary(getPreferenceScreen().getSharedPreferences(), findPreference(POCKET_ACCOUNT));
                return true;
            } else if (preference.getKey().equals(FORCE_REFRESH)) {
                int appWidgetId = getPreferenceScreen().getSharedPreferences().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                refreshWidget(appWidgetId);
                Toast.makeText(getActivity(), getString(R.string.refreshing), Toast.LENGTH_LONG).show();
                return true;
            } else if (preference.getKey().equals(RATE_THIS)) {
                try {
                    Uri uri = Uri.parse("market://details?id=dev.emmaguy.pocketwidget");
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.failed_to_launch_market, Toast.LENGTH_SHORT).show();
                }
            }

            return false;
        }

        private void beginSignInProcessOrSignOut() {
            if (isSignedIn()) {
                getPreferenceScreen()
                        .getSharedPreferences()
                        .edit()
                        .remove(CODE)
                        .remove(ACCESS_TOKEN)
                        .remove(USERNAME)
                        .remove(UNREAD_COUNT)
                        .apply();
                Toast.makeText(getActivity(), getString(R.string.account_cleared), Toast.LENGTH_LONG).show();

                UnreadArticlesWidgetProvider.killService(getActivity());
            } else {
                new RetrieveRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
                        this, getPreferenceScreen().getSharedPreferences(), getActivity(),
                        getString(R.string.retrieving_request_token)).execute();
            }
        }

        private void refreshWidget(int appWidgetId) {
            new UnreadArticlesWidgetProvider().onUpdate(getActivity(), AppWidgetManager.getInstance(getActivity()), new int[]{appWidgetId});
        }

        @Override
        public void onRetrievedUrl(final String url) {
            redirectToBrowser(url);
        }

        private void redirectToBrowser(String url) {
            Toast.makeText(getActivity(), getString(R.string.redirecting_to_browser), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);
        }

        @Override
        public void onError(final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onRetrievedAccessToken(String accessToken, String username) {
            getPreferenceManager()
                    .getSharedPreferences()
                    .edit()
                    .putString(SettingsActivity.ACCESS_TOKEN, accessToken)
                    .putString(SettingsActivity.USERNAME, username)
                    .apply();

            updatePrefsSummary(getPreferenceScreen().getSharedPreferences(), findPreference(POCKET_ACCOUNT));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.successfully_logged_in_as) + " " + getPreferenceManager().getSharedPreferences().getString(USERNAME, null),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}