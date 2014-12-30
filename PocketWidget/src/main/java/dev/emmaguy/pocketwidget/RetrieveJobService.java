package dev.emmaguy.pocketwidget;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dev.emmaguy.pocketwidget.ui.SettingsActivity;
import dev.emmaguy.pocketwidget.widget.WidgetProvider;
import me.tatarka.support.job.JobParameters;
import me.tatarka.support.job.JobService;

public class RetrieveJobService extends JobService implements RetrieveCountOfUnreadArticlesAsyncTask.UnreadCountRetrievedListener {
    private boolean mFinishedRetrieving = false;

    private RetrieveCountOfUnreadArticlesAsyncTask mRetrieveCountOfUnreadArticlesAsyncTask;
    private JobParameters mJobParams;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mJobParams = jobParameters;

        Logger.Log("start job");
        final SharedPreferences sharedPreferences = getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_NAME, 0);
        final String accessToken = sharedPreferences.getString(SettingsActivity.POCKET_AUTH_ACCESS_TOKEN, null);

        if (!TextUtils.isEmpty(accessToken)) {
            mRetrieveCountOfUnreadArticlesAsyncTask = new RetrieveCountOfUnreadArticlesAsyncTask(getResources().getString(R.string.pocket_consumer_key_mobile), accessToken, this);
            mRetrieveCountOfUnreadArticlesAsyncTask.execute();

            return true;
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        boolean needsRescheduling = !mFinishedRetrieving;
        Logger.Log("stop job, returning: " + needsRescheduling);

        if (mRetrieveCountOfUnreadArticlesAsyncTask != null && needsRescheduling) {
            mRetrieveCountOfUnreadArticlesAsyncTask.cancel(true);
        }

        return needsRescheduling;
    }

    @Override
    public void onUnreadCountRetrieved(Integer unreadCount) {
        Logger.Log("onUnreadCountRetrieved: " + unreadCount);

        insertUnreadCount(this, unreadCount);

        // We're done - successfully retrieved the latest count, so no need to reschedule
        mFinishedRetrieving = true;
        mRetrieveCountOfUnreadArticlesAsyncTask = null;

        // Update any widgets
        WidgetProvider.updateAllWidgets(this);

        jobFinished(mJobParams, false);
    }

    public static void insertUnreadCount(Context c, int unreadCount) {
        if (unreadCount < 0) {
            return;
        }

        Date now = Calendar.getInstance().getTime();
        String formattedDate = DataProvider.sDateFormat.format(now.getTime());

        ContentValues v = new ContentValues();
        v.put(DataProvider.DATE, formattedDate);
        v.put(DataProvider.DATE_TICKS, now.getTime());
        v.put(DataProvider.UNREAD_COUNT, unreadCount);

        try {
            int entryId = getIdOfUnreadCountAtDate(c, formattedDate);
            Logger.Log("entryId " + entryId + " date " + formattedDate);
            if (entryId != -1) {
                Logger.Log("Updating to " + unreadCount);
                c.getContentResolver().update(DataProvider.UNREAD_ARTICLES_COUNT_URI, v, DataProvider.ID + " = ? ", new String[]{"" + entryId});
            } else {
                Logger.Log("Inserting " + unreadCount);
                c.getContentResolver().insert(DataProvider.UNREAD_ARTICLES_COUNT_URI, v);
            }
        } catch (Exception e) {
            Logger.Log("Failed to add entry", e);
        }
    }

    private static int getIdOfUnreadCountAtDate(Context c, String date) {
        Cursor cursor = c.getContentResolver().query(DataProvider.UNREAD_ARTICLES_COUNT_URI, new String[]{DataProvider.ID}, DataProvider.DATE + " = ? ", new String[]{date}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int entryId = cursor.getInt(cursor.getColumnIndex(DataProvider.ID));
                cursor.close();
                return entryId;
            }
        }
        return -1;
    }

    public static int getLatestUnreadCount(Context c) {
        Cursor cursor = c.getContentResolver().query(DataProvider.LATEST_UNREAD_COUNT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int unreadCount = cursor.getInt(cursor.getColumnIndex(DataProvider.UNREAD_COUNT));
                cursor.close();
                return unreadCount;
            }
        }
        return -1;
    }
}
