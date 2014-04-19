package dev.emmaguy.pocketwidget;

import android.content.Context;
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

public class RetrieveAccessTokenAsyncTask extends ProgressAsyncTask<Void, Void, Void> {

    private final String consumerKey;
    private final OnAccessTokenRetrievedListener accessTokenRetrievedListener;
    private final String code;

    private String accessToken;
    private String username;

    public RetrieveAccessTokenAsyncTask(String consumerKey, OnAccessTokenRetrievedListener listener, Context c, String dialogMessage, String code) {
        super(c, dialogMessage);

        this.consumerKey = consumerKey;
        this.accessTokenRetrievedListener = listener;
        this.code = code;
    }

    public interface OnAccessTokenRetrievedListener {
        void onRetrievedAccessToken(String accessToken, String username);
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {
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

            accessToken = jsonObj.get("access_token").getAsString();
            username = jsonObj.get("username").getAsString();
        } catch (Exception e) {
            Log.e("RetrieveRequestToken", "Failed to retrieve request token", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        super.onPostExecute(x);
        accessTokenRetrievedListener.onRetrievedAccessToken(accessToken, username);
    }
}