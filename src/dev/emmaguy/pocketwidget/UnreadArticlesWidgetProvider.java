package dev.emmaguy.pocketwidget;

import java.util.Calendar;

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

    private PendingIntent service = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

	final Calendar time = Calendar.getInstance();
	time.set(Calendar.MINUTE, 0);
	time.set(Calendar.SECOND, 0);
	time.set(Calendar.MILLISECOND, 0);

	final Intent serviceIntent = new Intent(context, RetrieveUnreadArticlesCountService.class);
	if (service == null) {
	    service = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	final String refreshIntervalStr = context.getSharedPreferences(
		UnreadArticlesPreferenceActivity.SHARED_PREFERENCES, 0).getString("refresh_interval", "1");
	final int intervalInHours = Integer.valueOf(refreshIntervalStr);
	Log.i("xx", "refresh in " + intervalInHours + " hrs");
	m.setRepeating(AlarmManager.RTC, time.getTime().getTime(), AlarmManager.INTERVAL_HOUR * intervalInHours,
		service);
    }

    @Override
    public void onDisabled(Context context) {
	final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	m.cancel(service);
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