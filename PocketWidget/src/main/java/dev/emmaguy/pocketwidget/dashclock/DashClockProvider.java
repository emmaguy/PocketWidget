package dev.emmaguy.pocketwidget.dashclock;

import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import dev.emmaguy.pocketwidget.R;
import dev.emmaguy.pocketwidget.RetrieveJobService;
import dev.emmaguy.pocketwidget.widget.WidgetProvider;

public class DashClockProvider extends DashClockExtension {

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