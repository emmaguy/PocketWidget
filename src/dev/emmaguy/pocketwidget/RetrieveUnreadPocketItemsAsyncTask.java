package dev.emmaguy.pocketwidget;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.appwidget.AppWidgetManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RetrieveUnreadPocketItemsAsyncTask extends AsyncTask<Void, Void, Integer> {

    private final AppWidgetManager appWidgetManager;
    private final RemoteViews remoteViews;
    private final SharedPreferences sharedPreferences;
    private final String consumerKey;
    private final int widgetId;

    public RetrieveUnreadPocketItemsAsyncTask(RemoteViews remoteViews, int widgetId, AppWidgetManager appWidgetManager,
	    SharedPreferences sharedPreferences, String consumerKey) {
	this.remoteViews = remoteViews;
	this.widgetId = widgetId;
	this.appWidgetManager = appWidgetManager;
	this.sharedPreferences = sharedPreferences;
	this.consumerKey = consumerKey;
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
	
	final String accessToken = sharedPreferences.getString("access_token", null);
	if(accessToken == null || accessToken.length() <= 0){
	    return -1;
	}

	HttpClient client = new DefaultHttpClient();
	HttpPost post = new HttpPost("https://getpocket.com/v3/get");
	post.setHeader(HTTP.CONTENT_TYPE, "application/json");
	post.setHeader("X-Accept", "application/json");

	try {
	    JSONObject holder = new JSONObject();
	    holder.put("consumer_key", consumerKey);
	    holder.put("access_token", accessToken);
	    holder.put("detailType", "simple");
	    holder.put("state", "unread");
	    post.setEntity(new StringEntity(holder.toString()));

	    HttpResponse response = client.execute(post);
	    String responseBody = EntityUtils.toString(response.getEntity());
	    
	    final JsonElement parse = new JsonParser().parse(responseBody);
	    final JsonObject asJsonObject = parse.getAsJsonObject();
	    final JsonElement jsonElement = asJsonObject.get("list");
	    final JsonObject listItems = jsonElement.getAsJsonObject();
	    
	    return listItems.entrySet().size();
	} catch (Exception e) {
	    Log.e("RetrieveUnreadItems", "Failed to retrieve request token" + e.getMessage());

	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    Log.e("RetrieveUnreadItems", sw.toString());
	}

	return -1;
    }

    @Override
    protected void onPostExecute(Integer unreadCount) {
	if (unreadCount > 0) {
	    this.remoteViews.setViewVisibility(R.id.unread_count_textview, View.VISIBLE);
	    this.remoteViews.setTextViewText(R.id.unread_count_textview, unreadCount.toString());
	    this.appWidgetManager.updateAppWidget(widgetId, remoteViews);
	}
    }
}
