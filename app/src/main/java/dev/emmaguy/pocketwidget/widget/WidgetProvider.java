package dev.emmaguy.pocketwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;
import dev.emmaguy.pocketwidget.ui.SettingsActivity;

public class WidgetProvider extends AppWidgetProvider {

    public static final String POCKET_PACKAGE_NAME = "com.ideashower.readitlater.pro";
    private static final String WIDGET_EVENT = "WIDGET_EVENT";

    public static void updateAllWidgets(final Context context) {
        int unreadCount = RetrieveJobService.getLatestUnreadCount(context);

        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        int[] allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);

        for (int appWidgetId : allWidgetIds) {
            updateWidgetId(context, unreadCount, appWidgetId);
        }
    }

    public static void updateWidgetId(Context context, int appWidgetId) {
        int unreadCount = RetrieveJobService.getLatestUnreadCount(context);

        updateWidgetId(context, unreadCount, appWidgetId);
    }

    private static void updateWidgetId(Context context, int unreadCount, int appWidgetId) {
        Intent clickIntent = new Intent(context, WidgetProvider.class);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_imageview, pendingIntent);

        if (unreadCount >= 0) {
            views.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
            views.setTextViewText(R.id.unread_count_textview, Integer.valueOf(unreadCount).toString());
        } else {
            views.setViewVisibility(R.id.unread_count_textview, View.GONE);
        }

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAllWidgets(context);
    }

    @Override public void onDisabled(Context context) {
    }

    @Override public void onReceive(Context context, final Intent intent) {
        if (intent.getAction() == null) {
            // user has pressed on the widget
            Bundle extras = intent.getExtras();
            if (extras != null) {
                PackageManager pm = context.getPackageManager();
                try {
                    final Intent i = pm.getLaunchIntentForPackage(POCKET_PACKAGE_NAME);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(i);

                    //AnalyticsTracker.sendEvent(AnalyticsTracker.LOG_OPEN_POCKET_APP);
                } catch (Exception e) {
                    //AnalyticsTracker.sendThrowable("Failed to open Pocket app", e);
                }
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            updateAllWidgets(context);
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            if (!getSharedPreferences(context).getBoolean(WIDGET_EVENT, false)) {
                //AnalyticsTracker.sendEvent(AnalyticsTracker.LOG_WIDGET_EVENT);
                getSharedPreferences(context).edit().putBoolean(WIDGET_EVENT, true).apply();
            }
        }
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
