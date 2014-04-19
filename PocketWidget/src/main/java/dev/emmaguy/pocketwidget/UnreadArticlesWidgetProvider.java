package dev.emmaguy.pocketwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class UnreadArticlesWidgetProvider extends AppWidgetProvider {

    private static PendingIntent service = null;
    private static final String DEFAULT_REFRESH_HOURS = "3";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        createOrUpdateService(
                context,
                context.getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, 0).getString(SettingsActivity.REFRESH_INTERVAL, DEFAULT_REFRESH_HOURS)
        );
    }

    public static void createOrUpdateService(Context context, String refreshInterval) {
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        final Intent serviceIntent = new Intent(context, RetrieveUnreadArticlesCountService.class);
        if (service == null) {
            service = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        final int intervalInHours = Integer.valueOf(refreshInterval);
        m.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR * intervalInHours, service);
    }

    @Override
    public void onDisabled(Context context) {
        killService(context);
    }

    public static void killService(Context context) {
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        m.cancel(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            // user has pressed on the widget
            Bundle extras = intent.getExtras();
            if (extras != null) {
                PackageManager pm = context.getPackageManager();
                try {
                    String packageName = "com.ideashower.readitlater.pro";
                    Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
                    context.startActivity(launchIntent);
                } catch (Exception e) {
                    Log.e("OnReceive", "Failed to open Pocket app: " + e.getMessage());
                }
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            createOrUpdateService(
                    context,
                    context.getSharedPreferences(SettingsActivity.SHARED_PREFERENCES, 0).getString(SettingsActivity.REFRESH_INTERVAL, DEFAULT_REFRESH_HOURS));
        }
    }
}