package dev.emmaguy.pocketwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class PocketWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	for (int appWidgetId : appWidgetIds) {
	    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

	    Intent clickIntent = new Intent(context, PocketWidgetProvider.class);
	    clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
	    views.setOnClickPendingIntent(R.id.widget_imageview, pendingIntent);

	    Log.i("PocketWidgetConfigure", "OnUpdate: " +  appWidgetId);
	    
	    appWidgetManager.updateAppWidget(appWidgetId, views);
	    this.onReceive(context, clickIntent);
	}
	super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	if (intent.getAction() == null) {
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
		int widgetId = extras
			.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		Log.i("PocketWidgetConfigure", "OnReceive: " + widgetId);
		new RetrieveUnreadPocketItemsAsyncTask(remoteViews, widgetId, AppWidgetManager.getInstance(context), context.getSharedPreferences(PocketWidgetConfigure.SHARED_PREFERENCES, 0), context.getResources().getString(R.string.pocket_consumer_key_mobile))
			.execute();
	    }
	}
	
	super.onReceive(context, intent);
    }
}