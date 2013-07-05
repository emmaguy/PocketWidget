package dev.emmaguy.pocketwidget;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RetrieveRequestTokenAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String CALLBACK_URL = "pocketwidget://callback";
    
    private final String consumerKey;
    private final SharedPreferences sharedPreferences;
    private final OnUrlRetrievedListener retreivedUrlListener;

    public RetrieveRequestTokenAsyncTask(String consumerKey, OnUrlRetrievedListener onUrlRetrievedListener, SharedPreferences sharedPreferences) {
	this.consumerKey = consumerKey;
	this.retreivedUrlListener = onUrlRetrievedListener;
	this.sharedPreferences = sharedPreferences;
    }
    
    public interface OnUrlRetrievedListener{
	void onRetrievedUrl(String str);	
    }
    
    @Override
    protected String doInBackground(Void... params) {

	try {
	    
	    String token = getRequestToken();
	    sharedPreferences.edit().putString("code", token).commit();
	    return String.format("https://getpocket.com/auth/authorize?request_token=%s&redirect_uri=%s", token, CALLBACK_URL);
	    
	} catch (Exception e) {
	    Log.e("RetrieveRequestToken", "Failed to retrieve request token" + e.getMessage());

	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    Log.e("RetrieveRequestToken", sw.toString());
	}

	return null;
    }

    private String getRequestToken() throws UnsupportedEncodingException, IOException, ClientProtocolException, JSONException {
	HttpClient client = new DefaultHttpClient();
	HttpPost post = new HttpPost("https://getpocket.com/v3/oauth/request");
	post.setHeader(HTTP.CONTENT_TYPE, "application/json");
	post.setHeader("X-Accept", "application/json");
	
	JSONObject holder = new JSONObject();
	holder.put("consumer_key", consumerKey);
	holder.put("redirect_uri", CALLBACK_URL);
	post.setEntity(new StringEntity(holder.toString()));

	HttpResponse response = client.execute(post);
	String responseBody = EntityUtils.toString(response.getEntity());

	JsonObject jsonObj = new JsonParser().parse(responseBody).getAsJsonObject();
	return jsonObj.get("code").getAsString();
    }

    @Override
    protected void onPostExecute(String str) {
	if(str != null && str.length() > 0 ) {
	    retreivedUrlListener.onRetrievedUrl(str);
	}
    }
}