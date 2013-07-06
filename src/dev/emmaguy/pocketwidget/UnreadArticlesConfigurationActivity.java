package dev.emmaguy.pocketwidget;

import dev.emmaguy.pocketwidget.RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener;
import dev.emmaguy.pocketwidget.RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class UnreadArticlesConfigurationActivity extends Activity implements View.OnClickListener, OnUrlRetrievedListener,
	OnAccessTokenRetrievedListener {

    public static final String SHARED_PREFERENCES = "pocketWidget";

    private SharedPreferences sharedPreferences;
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_configure);

	sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, 0);
	
	Intent intent = getIntent();
	Bundle extras = intent.getExtras();

	if (extras != null) {
	    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

	    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
		sharedPreferences.edit().putInt("appWidgetId", appWidgetId).commit();
		
		findViewById(R.id.login_button).setOnClickListener(this);
	    } else {
		appWidgetId = sharedPreferences.getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
	    }
	}

	if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	    Log.i("PocketWidgetConfigure", "Invalid appWidgetId found");
	    finish();
	}

	String accessToken = sharedPreferences.getString("access_token", null);
	if (accessToken != null && accessToken.length() > 0) {
	    Log.i("PocketWidgetConfigure", "Token found in shared prefs");
	    refreshAndFinishActivity();
	    return;
	}
    }

    @Override
    public void onClick(View view) {
	Log.i("d", "clicked");
	if (view.getId() == R.id.login_button) {
	    Log.i("d", "clicked2");
	    findViewById(R.id.login_button).setVisibility(View.GONE);
	    Toast.makeText(getApplicationContext(), "Requesting token from Pocket", Toast.LENGTH_SHORT).show();
	    new RetrieveRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnUrlRetrievedListener) this, sharedPreferences).execute();
	}
    }

    @Override
    public void onRetrievedUrl(String url) {
	Toast.makeText(getApplicationContext(), "Redirecting to browser for authentication", Toast.LENGTH_SHORT).show();
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, intent);
	finish();
	startActivity(intent);
    }

    @Override
    public void onResume() {
	super.onResume();

	Uri uri = this.getIntent().getData();
	if (uri != null && uri.toString().startsWith("pocketwidget")) {
	    Toast.makeText(getApplicationContext(), "Verifying access to Pocket", Toast.LENGTH_SHORT).show();
	    new RetrieveAccessTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnAccessTokenRetrievedListener) this, sharedPreferences).execute();
	}
    }

    @Override
    public void onRetrievedAccessToken() {
	refreshAndFinishActivity();
    }

    private void refreshAndFinishActivity() {
	Log.i("PocketWidgetConfigure", "Refresh and finish: " + appWidgetId);
	refreshWidget();

	showHomeScreenAndFinish();
    }

    private void refreshWidget() {
	new UnreadArticlesWidgetProvider()
	  .onUpdate(this,
	            AppWidgetManager.getInstance(this),
	            new int[] { appWidgetId }
	   );
    }

    private void showHomeScreenAndFinish() {
	Intent showHome = new Intent(Intent.ACTION_MAIN);
	showHome.addCategory(Intent.CATEGORY_HOME);
	showHome.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, showHome);
	finish();
	startActivity(showHome);
    }
}
