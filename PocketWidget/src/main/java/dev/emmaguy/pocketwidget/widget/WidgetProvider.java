package dev.emmaguy.pocketwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import dev.emmaguy.pocketwidget.Logger;
import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;

public class WidgetProvider extends AppWidgetProvider {
    public static final String POCKET_PACKAGE_NAME = "com.ideashower.readitlater.pro";

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
        views.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
        views.setTextViewText(R.id.unread_count_textview, Integer.valueOf(unreadCount).toString());

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAllWidgets(context);
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            // user has pressed on the widget
            Bundle extras = intent.getExtras();
            if (extras != null) {
                PackageManager pm = context.getPackageManager();
                try {
                    Intent launchIntent = pm.getLaunchIntentForPackage(POCKET_PACKAGE_NAME);
                    context.startActivity(launchIntent);
                } catch (Exception e) {
                    Logger.Log("Failed to open Pocket app", e);
                }
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            updateAllWidgets(context);
        }
    }
}