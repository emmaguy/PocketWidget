package dev.emmaguy.pocketwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PocketWidgetProvider extends AppWidgetProvider
{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {   	
        for(int appWidgetId : appWidgetIds)
        {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Intent clickIntent = new Intent(context, PocketWidgetProvider.class);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clickIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_imageview, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() == null)
        {
            Bundle extras = intent.getExtras();
            if(extras != null)
            {
//                int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        	Toast.makeText(context, "onreceive", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onReceive(context, intent);
        }
    }
}