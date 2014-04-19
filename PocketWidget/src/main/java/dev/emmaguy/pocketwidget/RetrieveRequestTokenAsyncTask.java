package dev.emmaguy.pocketwidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class RetrieveRequestTokenAsyncTask extends ProgressAsyncTask<Void, Void, String> {

    private static final String CALLBACK_URL = "pocketwidget://callback";

    private final String consumerKey;
    private final SharedPreferences sharedPreferences;
    private final OnUrlRetrievedListener retrievedUrlListener;

    public RetrieveRequestTokenAsyncTask(String consumerKey, OnUrlRetrievedListener onUrlRetrievedListener,
                                         SharedPreferences sharedPreferences, Context c, String dialogMessage) {
        super(c, dialogMessage);

        this.consumerKey = consumerKey;
        this.retrievedUrlListener = onUrlRetrievedListener;
        this.sharedPreferences = sharedPreferences;
    }

    public interface OnUrlRetrievedListener {
        void onRetrievedUrl(String str);
        void onError(String message);
    }

    @Override
    protected String doInBackground(Void... params) {
        String token = getRequestToken();
        if (token != null && token.length() > 0) {
            sharedPreferences.edit().putString(SettingsActivity.CODE, token).apply();
            return String.format("https://getpocket.com/auth/authorize?request_token=%s&redirect_uri=%s", token, CALLBACK_URL);
        }
        return null;
    }

    private String getRequestToken() {
        try {
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

        } catch (Exception e) {
            retrievedUrlListener.onError("Failed to retrieve request token: " + e.getMessage());
            Log.e("RetrieveRequestToken", "Failed to retrieve request token", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        if (str != null && str.length() > 0) {
            retrievedUrlListener.onRetrievedUrl(str);
        }
    }
}