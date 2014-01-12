package dev.emmaguy.pocketwidget;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class RetrieveCountOfUnreadArticlesAsyncTask extends AsyncTask<Void, Void, Integer> {

    private final String consumerKey;
    private final String accessToken;
    private final UnreadCountRetrievedListener listener;

    public RetrieveCountOfUnreadArticlesAsyncTask(String consumerKey, String accessToken, UnreadCountRetrievedListener listener) {
        this.consumerKey = consumerKey;
        this.accessToken = accessToken;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://getpocket.com/v3/stats");
        post.setHeader(HTTP.CONTENT_TYPE, "application/json");
        post.setHeader("X-Accept", "application/json");

        try {
            JSONObject holder = new JSONObject();
            holder.put("consumer_key", consumerKey);
            holder.put("access_token", accessToken);
            post.setEntity(new StringEntity(holder.toString()));

            HttpResponse response = client.execute(post);
            String responseBody = EntityUtils.toString(response.getEntity());

            final JsonElement parse = new JsonParser().parse(responseBody);

            int unreadCount = parse.getAsJsonObject().get("count_unread").getAsInt();
            return unreadCount;
        } catch (Exception e) {
            Log.e("UnreadArticlesWidget", "Failed to get unread items", e);
        }

        return -1;
    }

    public interface UnreadCountRetrievedListener {
        void onUnreadCountRetrieved(Integer unreadCount);
    }

    @Override
    protected void onPostExecute(Integer unreadCount) {
        listener.onUnreadCountRetrieved(unreadCount);
    }
}
