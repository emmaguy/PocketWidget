package dev.emmaguy.pocketwidget;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class RetrievePocketApiRequestTokenAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String CALLBACK_URL = "pocketwidget://callback";

    private final String consumerKey;
    private SharedPreferences sharedPreferences;

    public RetrievePocketApiRequestTokenAsyncTask(String consumerKey, SharedPreferences sharedPreferences) {
	this.consumerKey = consumerKey;
	this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected String doInBackground(Void... params) {

	String code = "";
	try {
	    HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("https://getpocket.com/v3/oauth/request");
            
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("consumer_key", consumerKey));
            pairs.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));   
            post.setEntity(new UrlEncodedFormEntity(pairs));
            
            HttpResponse response = client.execute(post);
            for(Header h : response.getAllHeaders()) {
        	Log.e("sads", h.getName() + " v: " + h.getValue());
            }
            String responseBody = EntityUtils.toString(response.getEntity());
	    code = responseBody.substring(responseBody.indexOf('=') + 1);
	} catch (Exception e) {
	    Log.e("RetrieveRequestToken", "Failed to retrieve request token" + e.getMessage());
	    
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    Log.e("RetrieveRequestToken", sw.toString());
	}

	return code;
    }
    
    @Override
    protected void onPostExecute(String code) {
	if(code != null && code.length() > 0) {
	    sharedPreferences.edit().putString("code", code);
	}
    }
}