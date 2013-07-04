package dev.emmaguy.pocketwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

	    Log.i("PocketWidgetConfigure", "OnUpdate: " + appWidgetId);
	    new RetrieveUnreadPocketItemsAsyncTask(views, appWidgetId, AppWidgetManager.getInstance(context),
		    context.getSharedPreferences(PocketWidgetConfigure.SHARED_PREFERENCES, 0), context.getResources()
			    .getString(R.string.pocket_consumer_key_mobile)).execute();
	}
	super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	if (intent.getAction() == null) {
	    Bundle extras = intent.getExtras();
	    if (extras != null) {
		Log.i("PocketWidgetConfigure", "Opening the Pocket app");

		PackageManager pm = context.getPackageManager();
		try
		{
		    String packageName = "com.ideashower.readitlater.pro";
		    Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		    context.startActivity(launchIntent);
		} catch(Exception e){
		    Log.e("OnReceive", "Failed to open Pocket app: " + e.getMessage());
		}
	    }
	}

	super.onReceive(context, intent);
    }
}