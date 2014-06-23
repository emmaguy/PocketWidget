package dev.emmaguy.pocketwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener, RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    // empty constructor
    public SettingsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(SettingsActivity.SHARED_PREFERENCES);

        addPreferencesFromResource(R.xml.preferences);

        parseWidgetExtras();
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initSummary();

        retrieveAccessToken();

        PreferenceScreen screen = (PreferenceScreen) findPreference(SettingsActivity.POCKET_ACCOUNT);
        if (screen != null) {
            screen.setOnPreferenceClickListener(this);
        }

        PreferenceScreen forceRefresh = (PreferenceScreen) findPreference(SettingsActivity.FORCE_REFRESH);
        if (forceRefresh != null) {
            forceRefresh.setOnPreferenceClickListener(this);
        }

        PreferenceScreen rateThis = (PreferenceScreen) findPreference(SettingsActivity.RATE_THIS);
        if (rateThis != null) {
            rateThis.setOnPreferenceClickListener(this);
        }

        ListPreference refresh = (ListPreference) findPreference(SettingsActivity.REFRESH_INTERVAL);
        if (refresh != null) {
            refresh.setOnPreferenceClickListener(this);
        }
    }

    private void parseWidgetExtras() {
        Bundle extras = getActivity().getIntent().getExtras();

        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                getPreferenceManager().getSharedPreferences().edit().putInt("appWidgetId", appWidgetId).apply();
            } else {
                appWidgetId = getPreferenceManager().getSharedPreferences().getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (!getPreferenceManager().getSharedPreferences().getBoolean("isInitialised" + appWidgetId, false)) {
                getPreferenceManager().getSharedPreferences().edit().putBoolean("isInitialised" + appWidgetId, true).apply();

                final Intent intent2 = getActivity().getIntent();
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                getActivity().setResult(Activity.RESULT_OK, intent2);
                getActivity().finish();
                startActivity(intent2);
            }
        }

        String accessToken = getPreferenceManager().getSharedPreferences().getString(SettingsActivity.ACCESS_TOKEN, null);
        if (accessToken != null && accessToken.length() > 0 && appWidgetId >= 0) {
            refreshWidget(appWidgetId);
            return;
        }
    }

    private void retrieveAccessToken() {
        Uri uri = getActivity().getIntent().getData();
        if (uri != null && uri.toString().startsWith("pocketwidget") && !isSignedIn()) {
            String code = getPreferenceManager().getSharedPreferences().getString(SettingsActivity.CODE, null);

            new RetrieveAccessTokenAsyncTask(getString(R.string.pocket_consumer_key_mobile), this, getActivity(), getString(R.string.retrieving_access_token), code).execute();
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

        if (key.equals(SettingsActivity.REFRESH_INTERVAL)) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
            ComponentName thisWidget = new ComponentName(getActivity(), UnreadArticlesWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            if (allWidgetIds != null && allWidgetIds.length > 0) {
                UnreadArticlesWidgetProvider.createOrUpdateService(getActivity(), getPreferenceManager().getSharedPreferences().getString(key, null));
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
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) pref;

            if (checkBoxPreference.hasKey() && checkBoxPreference.getKey().equals(SettingsActivity.WIFI_ONLY_KEY)) {
                if (checkBoxPreference.isChecked()) {
                    checkBoxPreference.setSummary(R.string.auto_sync_summary_wifi_only);
                } else {
                    checkBoxPreference.setSummary(R.string.auto_sync_summary_wifi_and_mobile);
                }
            } else if(checkBoxPreference.hasKey() && checkBoxPreference.getKey().equals(SettingsActivity.ALWAYS_SHOW_KEY)) {
                if (checkBoxPreference.isChecked()) {
                    checkBoxPreference.setSummary(R.string.always_show_on_dashclock);
                } else {
                    checkBoxPreference.setSummary(R.string.dont_always_show_on_dashclock);
                }
            }
        } else if (pref instanceof PreferenceScreen) {
            PreferenceScreen screen = (PreferenceScreen) pref;

            if (screen.hasKey() && screen.getKey().equals(SettingsActivity.POCKET_ACCOUNT)) {
                if (isSignedIn()) {
                    screen.setSummary(getString(R.string.sign_out) + " " + sharedPreferences.getString(SettingsActivity.USERNAME, null));
                } else {
                    screen.setSummary(R.string.tap_to_sign_in);
                }
            }
        }
    }

    private boolean isSignedIn() {
        String accessToken = getPreferenceScreen().getSharedPreferences().getString(SettingsActivity.ACCESS_TOKEN, null);
        return accessToken != null && accessToken.length() > 0;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(SettingsActivity.POCKET_ACCOUNT)) {
            beginSignInProcessOrSignOut();
            updatePrefsSummary(getPreferenceScreen().getSharedPreferences(), findPreference(SettingsActivity.POCKET_ACCOUNT));
            return true;
        } else if (preference.getKey().equals(SettingsActivity.FORCE_REFRESH)) {
            int appWidgetId = getPreferenceScreen().getSharedPreferences().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            refreshWidget(appWidgetId);
            Toast.makeText(getActivity(), getString(R.string.refreshing), Toast.LENGTH_LONG).show();
            return true;
        } else if (preference.getKey().equals(SettingsActivity.RATE_THIS)) {
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
                    .remove(SettingsActivity.CODE)
                    .remove(SettingsActivity.ACCESS_TOKEN)
                    .remove(SettingsActivity.USERNAME)
                    .remove(SettingsActivity.UNREAD_COUNT)
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
        getActivity().finish();
        startActivity(intent);
    }

    @Override
    public void onError(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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

        updatePrefsSummary(getPreferenceScreen().getSharedPreferences(), findPreference(SettingsActivity.POCKET_ACCOUNT));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        getString(R.string.successfully_logged_in_as) + " " + getPreferenceManager().getSharedPreferences().getString(SettingsActivity.USERNAME, null),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
