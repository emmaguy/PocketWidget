package dev.emmaguy.pocketwidget.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_KEY_FROM_LAUNCHER, true);
        startActivity(intent);
        finish();
    }
}
