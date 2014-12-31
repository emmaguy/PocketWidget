package dev.emmaguy.pocketwidget.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import dev.emmaguy.pocketwidget.DataProvider;
import dev.emmaguy.pocketwidget.Logger;
import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveCountOfUnreadArticlesAsyncTask;
import dev.emmaguy.pocketwidget.RetrieveJobService;
import dev.emmaguy.pocketwidget.auth.RetrieveAccessTokenAsyncTask;
import dev.emmaguy.pocketwidget.auth.RetrieveRequestTokenAsyncTask;
import dev.emmaguy.pocketwidget.widget.WidgetProvider;
import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobScheduler;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener,
        RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener, RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener, RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener {
    private static final int RETRIEVE_UNREAD_ARTICLES_JOB_ID = 1;

    private static final String APP_WIDGET_ID = "appWidgetId";
    private static final String IS_INITIALISED = "isInitialised";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ProgressDialog mProgressDialog;

    // empty constructor
    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(SettingsActivity.SHARED_PREFERENCES_NAME);

        addPreferencesFromResource(R.xml.preferences);

        parseWidgetExtras();

        scheduleJob(getSharedPreferences());
    }

    @Override
    public void onResume() {
        super.onResume();

        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        initSummary();

        retrieveAccessToken();

        initialiseOnClickListener(getString(R.string.pref_key_pocket_account));
        initialiseOnClickListener(getString(R.string.pref_key_force_refresh));
        initialiseOnClickListener(getString(R.string.pref_key_rate_this));
        initialiseOnClickListener(getString(R.string.pref_key_view_graph));
    }

    private void initialiseOnClickListener(String string) {
        PreferenceScreen screen = (PreferenceScreen) findPreference(string);
        if (screen != null) {
            screen.setOnPreferenceClickListener(this);
        }
    }

    private void parseWidgetExtras() {
        Bundle extras = getActivity().getIntent().getExtras();

        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                getPreferenceManager().getSharedPreferences().edit().putInt(APP_WIDGET_ID, mAppWidgetId).apply();
            } else {
                mAppWidgetId = getPreferenceManager().getSharedPreferences().getInt(APP_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }

            if (!getPreferenceManager().getSharedPreferences().getBoolean(IS_INITIALISED + mAppWidgetId, false)) {
                getPreferenceManager().getSharedPreferences().edit().putBoolean(IS_INITIALISED + mAppWidgetId, true).apply();

                final Intent intent2 = getActivity().getIntent();
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                getActivity().setResult(Activity.RESULT_OK, intent2);
                getActivity().finish();
                startActivity(intent2);
            }
        }

        String accessToken = getPreferenceManager().getSharedPreferences().getString(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN, null);
        if (accessToken != null && accessToken.length() > 0 && mAppWidgetId >= 0) {
            WidgetProvider.updateWidgetId(getActivity(), mAppWidgetId);
            return;
        }
    }

    private void retrieveAccessToken() {
        Uri uri = getActivity().getIntent().getData();
        if (uri != null && uri.toString().startsWith("pocketwidget") && !isSignedIn()) {
            String code = getPreferenceManager().getSharedPreferences().getString(SettingsActivity.POCKET_AUTH_CODE, null);

            mProgressDialog = showProgressDialog(getString(R.string.retrieving_access_token));
            new RetrieveAccessTokenAsyncTask(getString(R.string.pocket_consumer_key_mobile), this, code).execute();
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

        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefsSummary(sharedPreferences, findPreference(key));

        if (key.equals(getString(R.string.pref_key_refresh_interval_minutes))) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
            ComponentName thisWidget = new ComponentName(getActivity(), WidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            if (allWidgetIds != null && allWidgetIds.length > 0) {
                scheduleJob(sharedPreferences);
            }
        }
    }

    private void scheduleJob(SharedPreferences prefs) {
        cancelRetrieveJob();

        boolean canSyncOnWifiOnly = prefs.getBoolean(getString(R.string.pref_key_wifi_only), false);
        String refreshInterval = prefs.getString(getString(R.string.pref_key_refresh_interval_minutes), getString(R.string.pref_default_refresh_interval_minutes));

        int refreshIntervalMins = Integer.valueOf(refreshInterval);

        Logger.Log("scheduleJob interval: " + refreshIntervalMins);

        JobScheduler jobScheduler = JobScheduler.getInstance(getActivity());

        JobInfo job = new JobInfo.Builder(RETRIEVE_UNREAD_ARTICLES_JOB_ID, new ComponentName(getActivity(), RetrieveJobService.class))
                .setPeriodic(TimeUnit.MINUTES.toMillis(refreshIntervalMins))
                .setRequiredNetworkType(canSyncOnWifiOnly ? JobInfo.NETWORK_TYPE_UNMETERED : JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        jobScheduler.schedule(job);
    }

    private void cancelRetrieveJob() {
        JobScheduler jobScheduler = JobScheduler.getInstance(getActivity());
        jobScheduler.cancelAll();
    }

    protected void updatePrefsSummary(SharedPreferences sharedPreferences, Preference pref) {
        if (pref == null)
            return;

        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry());
        } else if (pref instanceof PreferenceScreen) {
            PreferenceScreen screen = (PreferenceScreen) pref;

            if (screen.hasKey() && screen.getKey().equals(getString(R.string.pref_key_pocket_account))) {
                if (isSignedIn()) {
                    screen.setSummary(getString(R.string.sign_out) + " " + sharedPreferences.getString(SettingsActivity.POCKET_USERNAME, null));
                } else {
                    screen.setSummary(R.string.tap_to_sign_in);
                }
            }
        }
    }

    private boolean isSignedIn() {
        String accessToken = getSharedPreferences().getString(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN, null);
        return accessToken != null && accessToken.length() > 0;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.pref_key_pocket_account))) {
            beginSignInProcessOrSignOut();
            updatePrefsSummary(getSharedPreferences(), findPreference(getString(R.string.pref_key_pocket_account)));
            return true;
        } else if (preference.getKey().equals(getString(R.string.pref_key_force_refresh))) {
            if (isSignedIn()) {
                mProgressDialog = showProgressDialog(getString(R.string.retrieving_latest_unread_count));
// TODO just cancel?
                final String accessToken = getSharedPreferences().getString(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN, null);
                new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile), accessToken, this).execute();
            } else {
                Toast.makeText(getActivity(), R.string.please_login_first, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.pref_key_rate_this))) {
            try {
                Uri uri = Uri.parse("market://details?id=dev.emmaguy.pocketwidget");
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.failed_to_launch_market, Toast.LENGTH_SHORT).show();
            }
        } else if (preference.getKey().equals(getString(R.string.pref_key_view_graph))) {
            startActivity(new Intent(getActivity(), GraphActivity.class));
        }

        return false;
    }

    private void beginSignInProcessOrSignOut() {
        if (isSignedIn()) {
            getSharedPreferences()
                    .edit()
                    .remove(SettingsActivity.POCKET_AUTH_CODE)
                    .remove(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN)
                    .remove(SettingsActivity.POCKET_USERNAME)
                    .apply();
            Toast.makeText(getActivity(), getString(R.string.account_cleared), Toast.LENGTH_LONG).show();

            getActivity().getContentResolver().delete(DataProvider.UNREAD_ARTICLES_COUNT_URI, null, null);
            cancelRetrieveJob();
        } else {
            mProgressDialog = showProgressDialog(getString(R.string.retrieving_request_token));
            new RetrieveRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
                    this, getSharedPreferences()).execute();
        }
    }

    private ProgressDialog showProgressDialog(String message) {
        return ProgressDialog.show(getActivity(), "", message, true, false);
    }

    private void cancelProgressDialog() {
        mProgressDialog.cancel();
        mProgressDialog = null;
    }

    @Override
    public void onRetrievedUrl(final String url) {
        cancelProgressDialog();

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
        cancelProgressDialog();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRetrievedAccessToken(String accessToken, String username) {
        cancelProgressDialog();

        getSharedPreferences()
                .edit()
                .putString(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN, accessToken)
                .putString(SettingsActivity.POCKET_USERNAME, username)
                .apply();

        updatePrefsSummary(getSharedPreferences(), findPreference(getString(R.string.pref_key_pocket_account)));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        getString(R.string.successfully_logged_in_as) + " " + getPreferenceManager().getSharedPreferences().getString(SettingsActivity.POCKET_USERNAME, null),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private SharedPreferences getSharedPreferences() {
        return getPreferenceScreen().getSharedPreferences();
    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {
        cancelProgressDialog();

        RetrieveJobService.insertUnreadCount(getActivity(), unreadCount);
    }
}
