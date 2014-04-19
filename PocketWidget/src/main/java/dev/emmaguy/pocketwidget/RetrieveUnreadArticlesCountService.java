package dev.emmaguy.pocketwidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;

import dev.emmaguy.pocketwidget.RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener;

public class RetrieveUnreadArticlesCountService extends Service implements UnreadCountRetrievedListener {

    @Override
    public void onStart(Intent intent, int startId) {
        final SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, 0);
        final String accessToken = sharedPreferences.getString(SettingsActivity.ACCESS_TOKEN, null);

        if (accessToken == null || accessToken.length() <= 0) {
            return;
        }

        // update the ui with what's stored in sharedprefs until the refresh value is available
        updateWidget(sharedPreferences.getInt(SettingsActivity.UNREAD_COUNT, 0));

        boolean syncOnWifiOnly = sharedPreferences.getBoolean(SettingsActivity.WIFI_ONLY, false);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected() || !syncOnWifiOnly) {
            new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile), accessToken, this).execute();
        }
    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {

        final SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, 0);

        if (unreadCount >= 0) {
            sharedPreferences.edit().putInt(SettingsActivity.UNREAD_COUNT, unreadCount).commit();

            updateWidget(unreadCount);
        }
    }

    private void updateWidget(final int unreadCount) {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        ComponentName thisWidget = new ComponentName(this, UnreadArticlesWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : allWidgetIds) {
            Intent clickIntent = new Intent(this, UnreadArticlesWidgetProvider.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_imageview, pendingIntent);
            views.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
            views.setTextViewText(R.id.unread_count_textview, Integer.valueOf(unreadCount).toString());

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
