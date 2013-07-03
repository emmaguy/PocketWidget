package dev.emmaguy.pocketwidget;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

public class RetrievePocketApiRequestTokenAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String CALLBACK_URL = "pocketwidget://callback";

    private final OAuthConsumer consumer;
    private final OAuthProvider provider = new DefaultOAuthProvider(
	    							"https://getpocket.com/v3/oauth/request",
	    							"https://getpocket.com/v3/oauth/authorize", 
	    							"https://getpocket.com/v3/oauth/authorize");

    public RetrievePocketApiRequestTokenAsyncTask(String consumerKey) {
	this.consumer = new CommonsHttpOAuthConsumer(consumerKey, "nosecret");
    }

    @Override
    protected String doInBackground(Void... params) {

	String authUrl = "";
	try {
	    authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
	} catch (Exception e) {
	    Log.e("RetrieveRequestToken", "Failed to retrieve request token" + e.getMessage());
	    
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    Log.e("RetrieveRequestToken", sw.toString());
	}

	return authUrl;
    }
    
    @Override
    protected void onPostExecute(String authUrl) {
	Log.e("RetrieveRequestToken", "AuthUrl: " + authUrl);
    }
}