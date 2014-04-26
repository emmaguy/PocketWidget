package dev.emmaguy.pocketwidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import dev.emmaguy.pocketwidget.RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener;

public class UnreadArticlesDashClockExtension extends DashClockExtension implements UnreadCountRetrievedListener {

    protected void onUpdateData(int reason) {
        final SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, 0);
        final String accessToken = sharedPreferences.getString(SettingsActivity.ACCESS_TOKEN, null);

        if (accessToken == null || accessToken.length() <= 0) {
            return;
        }

        boolean syncOnWifiOnly = sharedPreferences.getBoolean(SettingsActivity.WIFI_ONLY, false);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected() || !syncOnWifiOnly) {
            new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
                    accessToken, this).execute();
        }
    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {
        if (unreadCount > 0) {
            final PackageManager pm = getPackageManager();
            final Intent pocketAppIntent = pm.getLaunchIntentForPackage("com.ideashower.readitlater.pro");
            final ExtensionData extensionData = new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_launcher)
                    .status(unreadCount.toString())
                    .expandedTitle(getString(R.string.unread_items) + " " + unreadCount)
                    .clickIntent(pocketAppIntent);
            publishUpdate(extensionData);
        } else {
            publishUpdate(null);
        }
    }
}