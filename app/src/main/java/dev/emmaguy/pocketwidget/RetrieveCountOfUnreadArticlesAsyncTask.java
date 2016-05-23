package dev.emmaguy.pocketwidget;

import android.content.Context;
import android.os.AsyncTask;

//import com.google.gson.JsonElement;
//import com.google.gson.JsonParser;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

//import com.google.gson.JsonElement;
//import com.google.gson.JsonParser;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

public class RetrieveCountOfUnreadArticlesAsyncTask extends AsyncTask<Void, Void, Integer> {

    private final String mConsumerKey;
    private final String mAccessToken;
    private final UnreadCountRetrievedListener mListener;
    private final Context mContext;

    public RetrieveCountOfUnreadArticlesAsyncTask(String consumerKey,
            String accessToken,
            UnreadCountRetrievedListener listener,
            Context context) {
        mConsumerKey = consumerKey;
        mAccessToken = accessToken;
        mListener = listener;
        mContext = context;
    }

    @Override protected Integer doInBackground(Void... params) {
        //HttpClient client = new DefaultHttpClient();
        //HttpPost post = new HttpPost("https://getpocket.com/v3/stats");
        //post.setHeader(HTTP.CONTENT_TYPE, "application/json");
        //post.setHeader("X-Accept", "application/json");
        //
        //try {
        //    JSONObject holder = new JSONObject();
        //    holder.put("consumer_key", mConsumerKey);
        //    holder.put("access_token", mAccessToken);
        //    post.setEntity(new StringEntity(holder.toString()));
        //
        //    HttpResponse response = client.execute(post);
        //    String responseBody = EntityUtils.toString(response.getEntity());
        //
        //    final JsonElement parse = new JsonParser().parse(responseBody);
        //
        //    int unreadCount = parse.getAsJsonObject().get("count_unread").getAsInt();
        //    return unreadCount;
        //} catch (Exception e) {
        //    Logger.sendThrowable(mContext, "Failed to get unread items", e);
        //}

        return 20;
    }

    @Override protected void onPostExecute(Integer unreadCount) {
        mListener.onUnreadCountRetrieved(unreadCount);
    }

    public interface UnreadCountRetrievedListener {

        void onUnreadCountRetrieved(Integer unreadCount);
    }
}
