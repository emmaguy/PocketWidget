package dev.emmaguy.pocketwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class UnreadArticlesWidgetProvider extends AppWidgetProvider {
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	Intent intent = new Intent(context.getApplicationContext(), RetrieveUnreadArticlesCountService.class);
	context.startService(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
	if (intent.getAction() == null) {
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
	}
    }
}