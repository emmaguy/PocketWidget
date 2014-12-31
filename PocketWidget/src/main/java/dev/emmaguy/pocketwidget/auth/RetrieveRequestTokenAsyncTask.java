package dev.emmaguy.pocketwidget.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

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

import dev.emmaguy.pocketwidget.Logger;
import dev.emmaguy.pocketwidget.ui.SettingsActivity;

public class RetrieveRequestTokenAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String CALLBACK_URL = "pocketwidget://callback";

    private final String mConsumerKey;
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private final OnUrlRetrievedListener mRetrievedUrlListener;

    public RetrieveRequestTokenAsyncTask(String consumerKey, OnUrlRetrievedListener onUrlRetrievedListener, SharedPreferences sharedPreferences, Context context) {
        mConsumerKey = consumerKey;
        mRetrievedUrlListener = onUrlRetrievedListener;
        mSharedPreferences = sharedPreferences;
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        String token = getRequestToken();
        if (token != null && token.length() > 0) {
            mSharedPreferences.edit().putString(SettingsActivity.POCKET_AUTH_CODE, token).apply();
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
            holder.put("consumer_key", mConsumerKey);
            holder.put("redirect_uri", CALLBACK_URL);
            post.setEntity(new StringEntity(holder.toString()));

            HttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());

            JsonObject jsonObj = new JsonParser().parse(responseBody).getAsJsonObject();
            return jsonObj.get("code").getAsString();

        } catch (Exception e) {
            mRetrievedUrlListener.onError("Failed to retrieve request token: " + e.getMessage());
            Logger.sendThrowable(mContext, "Failed to retrieve request token", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
        if (str != null && str.length() > 0) {
            mRetrievedUrlListener.onRetrievedUrl(str);
        }
    }

    public interface OnUrlRetrievedListener {
        void onRetrievedUrl(String str);

        void onError(String message);
    }
}