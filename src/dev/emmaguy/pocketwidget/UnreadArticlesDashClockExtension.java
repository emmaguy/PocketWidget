package dev.emmaguy.pocketwidget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import dev.emmaguy.pocketwidget.RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener;

public class UnreadArticlesDashClockExtension extends DashClockExtension implements UnreadCountRetrievedListener {

    protected void onUpdateData(int reason) {
	final SharedPreferences sharedPreferences = getSharedPreferences(
		UnreadArticlesConfigurationActivity.SHARED_PREFERENCES, 0);
	final String accessToken = sharedPreferences.getString("access_token", null);

	if (accessToken == null || accessToken.length() <= 0) {
	    return;
	}

	Log.i("UnreadArticlesDashClockExtension", "onUpdateData");
	new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		accessToken, this).execute();

    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {
	if (unreadCount >= 0) {
	    final PackageManager pm = getPackageManager();
	    final Intent pocketAppIntent = pm.getLaunchIntentForPackage("com.ideashower.readitlater.pro");
	    final ExtensionData extensionData = new ExtensionData()

	    .visible(true).icon(R.drawable.ic_launcher).status(unreadCount.toString())
		    .expandedTitle("Unread Items: " + unreadCount).clickIntent(pocketAppIntent);

	    publishUpdate(extensionData);
	}
    }
}