package dev.emmaguy.pocketwidget.widget;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RemoteViews;

import dev.emmaguy.pocketwidget.AnalyticsTracker;
import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;

public class WidgetProvider extends AppWidgetProvider {

    public static final String POCKET_PACKAGE_NAME = "com.ideashower.readitlater.pro";

    public static void updateAllWidgets(@NonNull final Context context) {
        final int unreadCount = RetrieveJobService.getLatestUnreadCount(context);

        final ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
        final int[] allWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);

        for (final int appWidgetId : allWidgetIds) {
            updateWidgetId(context, unreadCount, appWidgetId);
        }
    }

    public static void updateWidgetId(@NonNull final Context context, final int appWidgetId) {
        final int unreadCount = RetrieveJobService.getLatestUnreadCount(context);

        updateWidgetId(context, unreadCount, appWidgetId);
    }

    private static void updateWidgetId(@NonNull final Context context, final int unreadCount, final int appWidgetId) {
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

    @Override public void onUpdate(@NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] appWidgetIds) {
        updateAllWidgets(context);
    }

    @Override public void onDisabled(@NonNull final Context context) {
    }

    @Override public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        final AnalyticsTracker analyticsTracker = new AnalyticsTracker(context);

        if (intent.getAction() == null) {
            // user has pressed on the widget
            final Bundle extras = intent.getExtras();
            if (extras != null) {
                final PackageManager pm = context.getPackageManager();
                try {
                    final Intent i = pm.getLaunchIntentForPackage(POCKET_PACKAGE_NAME);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(i);

                    analyticsTracker.sendEvent(FirebaseAnalytics.Event.APP_OPEN,
                            AnalyticsTracker.EVENT_OPEN_POCKET_APP);
                } catch (Exception e) {
                    FirebaseCrash.report(e);
                }
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            updateAllWidgets(context);
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
            analyticsTracker.sendEvent(AnalyticsTracker.EVENT_CATEGORY_GENERAL, AnalyticsTracker.EVENT_WIDGET_EVENT);
        }
    }
}
