package dev.emmaguy.pocketwidget;

import java.util.Random;

import android.appwidget.AppWidgetManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

public class RetrieveUnreadPocketItemsAsyncTask extends AsyncTask<Void, Void, Integer> {

    private final AppWidgetManager appWidgetManager;
    private final RemoteViews remoteViews;
    private final int widgetId;
    
    public RetrieveUnreadPocketItemsAsyncTask(RemoteViews remoteViews, int widgetId, AppWidgetManager appWidgetManager) {
	this.remoteViews = remoteViews;
	this.widgetId = widgetId;
	this.appWidgetManager = appWidgetManager;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {

	return new Random().nextInt(1000);
    }

    @Override
    protected void onPostExecute(Integer unreadCount) {
	this.remoteViews.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
	this.remoteViews.setTextViewText(R.id.unread_count_textview, unreadCount.toString());
	this.appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
