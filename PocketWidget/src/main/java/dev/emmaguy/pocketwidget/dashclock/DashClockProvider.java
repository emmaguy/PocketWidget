package dev.emmaguy.pocketwidget.dashclock;

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import dev.emmaguy.pocketwidget.Logger;
import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;
import dev.emmaguy.pocketwidget.ui.SettingsActivity;
import dev.emmaguy.pocketwidget.widget.WidgetProvider;

public class DashClockProvider extends DashClockExtension {
    public static final String DASHCLOCK_EVENT = "DASHCLOCK_EVENT";

    @Override
    public void onCreate() {
        super.onCreate();

        if (!getSharedPreferences().getBoolean(DASHCLOCK_EVENT, false)) {
            Logger.sendEvent(getApplicationContext(), Logger.LOG_DASHCLOCK_EVENT);
            getSharedPreferences().edit().putBoolean(DASHCLOCK_EVENT, true).apply();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(SettingsActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onUpdateData(int reason) {
        int unreadCount = RetrieveJobService.getLatestUnreadCount(this);

        if (unreadCount > 0) {
            final Intent pocketAppIntent = getPackageManager().getLaunchIntentForPackage(WidgetProvider.POCKET_PACKAGE_NAME);

            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_launcher)
                    .status("" + unreadCount)
                    .expandedTitle(getString(R.string.unread_articles_x, unreadCount))
                    .clickIntent(pocketAppIntent));
        } else if (unreadCount == 0) {
            publishUpdate(null);
        }
    }
}