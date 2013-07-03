package dev.emmaguy.pocketwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class PocketWidgetConfigure extends Activity implements View.OnClickListener, OnUrlRetrievedListener,
	OnAccessTokenRetrievedListener {

    private static final String SHARED_PREFERENCES = "pocketWidget";
    private int appWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.activity_configure);

	Intent intent = getIntent();
	Bundle extras = intent.getExtras();

	if (extras != null) {
	    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	}
	
	if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

	findViewById(R.id.login_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	if (view.getId() == R.id.login_button) {
	    Toast.makeText(getApplicationContext(), "Redirecting to retrieve request token", Toast.LENGTH_SHORT).show();
	    new RetrievePocketRequestTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnUrlRetrievedListener) this, getSharedPreferences(SHARED_PREFERENCES, 0), appWidgetId).execute();
	}
    }

    @Override
    public void onRetrievedUrl(String url) {
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	startActivity(intent);
    }

    @Override
    public void onResume() {
	super.onResume();

	Uri uri = this.getIntent().getData();
	if (uri != null && uri.toString().startsWith("pocketwidget")) {
	    Toast.makeText(getApplicationContext(), "Retrieving access token", Toast.LENGTH_SHORT).show();
	    new RetrievePocketAccessTokenAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnAccessTokenRetrievedListener) this, getSharedPreferences(SHARED_PREFERENCES, 0), appWidgetId).execute();
	}
    }

    @Override
    public void onRetrievedAccessToken() {
	Log.e("dawd", "dasd: " + appWidgetId + " token " + getSharedPreferences(SHARED_PREFERENCES, 0).getString("access_token" + appWidgetId, null));

	RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
	appWidgetManager.updateAppWidget(appWidgetId, views);

	Intent resultValue = new Intent();
	resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	setResult(RESULT_OK, resultValue);
	finish();
    }
}
