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

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RetrieveAccessTokenAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String consumerKey;
    private final SharedPreferences sharedPreferences;
    private final OnAccessTokenRetrievedListener accessTokenRetrievedListener;

    public RetrieveAccessTokenAsyncTask(String consumerKey, OnAccessTokenRetrievedListener listener,
	    SharedPreferences sharedPreferences) {
	this.consumerKey = consumerKey;
	this.sharedPreferences = sharedPreferences;
	this.accessTokenRetrievedListener = listener;
    }
    
    public interface OnAccessTokenRetrievedListener {
	    void onRetrievedAccessToken();
    }
    
    @Override
    protected Void doInBackground(Void... params) {

	try {

	    String token = getAccessToken();
	    sharedPreferences.edit().putString("access_token", token).commit();

	} catch (Exception e) {
	    Log.e("RetrieveRequestToken", "Failed to retrieve request token" + e.getMessage());

	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    Log.e("RetrieveRequestToken", sw.toString());
	}

	return null;
    }

    private String getAccessToken() throws Exception {

	String code = sharedPreferences.getString("code", null);

	if (code == null || code.length() <= 0) {
	    throw new Exception("Code (request token) is empty - reauthorisation is required.");
	}

	HttpClient client = new DefaultHttpClient();
	HttpPost post = new HttpPost("https://getpocket.com/v3/oauth/authorize");
	post.setHeader(HTTP.CONTENT_TYPE, "application/json");
	post.setHeader("X-Accept", "application/json");
	
	JSONObject holder = new JSONObject();
	holder.put("consumer_key", consumerKey);
	holder.put("code", code);
	post.setEntity(new StringEntity(holder.toString()));

	HttpResponse response = client.execute(post);
	String responseBody = EntityUtils.toString(response.getEntity());

	JsonObject jsonObj = new JsonParser().parse(responseBody).getAsJsonObject();
	return jsonObj.get("access_token").getAsString();
    }

    @Override
    protected void onPostExecute(Void x) {
	accessTokenRetrievedListener.onRetrievedAccessToken();
    }
}