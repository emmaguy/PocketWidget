package dev.emmaguy.pocketwidget.dashclock;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import android.content.Intent;
import android.content.pm.PackageManager;

import dev.emmaguy.pocketwidget.AnalyticsTracker;
import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;
import dev.emmaguy.pocketwidget.widget.WidgetProvider;

public class DashClockProvider extends DashClockExtension {
    private AnalyticsTracker analyticsTracker;

    @Override public void onCreate() {
        super.onCreate();

        analyticsTracker = new AnalyticsTracker(this);
        analyticsTracker.sendEvent(AnalyticsTracker.EVENT_CATEGORY_GENERAL, AnalyticsTracker.EVENT_DASHCLOCK_EVENT);
    }

    @Override protected void onUpdateData(final int reason) {
        final int unreadCount = RetrieveJobService.getLatestUnreadCount(this);
        final String unreadCountValue = "" + unreadCount;

        analyticsTracker.sendEvent(AnalyticsTracker.EVENT_CATEGORY_GENERAL, unreadCountValue);

        if (unreadCount > 0) {
            final PackageManager packageManager = getPackageManager();
            final Intent pocketAppIntent = packageManager.getLaunchIntentForPackage(WidgetProvider.POCKET_PACKAGE_NAME);

            publishUpdate(new ExtensionData().visible(true)
                    .icon(R.drawable.ic_launcher)
                    .status(unreadCountValue)
                    .expandedTitle(getString(R.string.unread_articles_x, unreadCount))
                    .clickIntent(pocketAppIntent));
        } else {
            publishUpdate(null);
        }
    }
}