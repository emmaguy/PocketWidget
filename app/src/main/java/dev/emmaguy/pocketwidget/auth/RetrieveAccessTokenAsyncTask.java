package dev.emmaguy.pocketwidget.auth;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import dev.emmaguy.pocketwidget.Logger;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

public class RetrieveAccessTokenAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String mCode;
    private final String mConsumerKey;
    private final Context mContext;
    private final OnAccessTokenRetrievedListener mAccessTokenRetrievedListener;

    private String mAccessToken;
    private String username;

    public RetrieveAccessTokenAsyncTask(String consumerKey,
            OnAccessTokenRetrievedListener listener,
            String code,
            Context context) {
        mConsumerKey = consumerKey;
        mAccessTokenRetrievedListener = listener;
        mCode = code;
        mContext = context;
    }

    @Override protected Void doInBackground(Void... params) {
        try {
            if (TextUtils.isEmpty(mCode)) {
                throw new Exception("Code (request token) is empty - re-authorisation is required.");
            }

            //HttpClient client = new DefaultHttpClient();
            //HttpPost post = new HttpPost("https://getpocket.com/v3/oauth/authorize");
            //post.setHeader(HTTP.CONTENT_TYPE, "application/json");
            //post.setHeader("X-Accept", "application/json");
            //
            //JSONObject holder = new JSONObject();
            //holder.put("consumer_key", mConsumerKey);
            //holder.put("code", mCode);
            //post.setEntity(new StringEntity(holder.toString()));
            //
            //HttpResponse response = client.execute(post);
            //String responseBody = EntityUtils.toString(response.getEntity());
            //
            //JsonObject jsonObj = new JsonParser().parse(responseBody).getAsJsonObject();
            //
            //mAccessToken = jsonObj.get("access_token").getAsString();
            //username = jsonObj.get("username").getAsString();
        } catch (Exception e) {
            Logger.sendThrowable(mContext, "Failed to retrieve request token", e);
        }

        return null;
    }

    @Override protected void onPostExecute(Void x) {
        mAccessTokenRetrievedListener.onRetrievedAccessToken(mAccessToken, username);
    }

    public interface OnAccessTokenRetrievedListener {

        void onRetrievedAccessToken(String accessToken, String username);
    }
}