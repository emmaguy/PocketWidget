package dev.emmaguy.pocketwidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	startActivity(new Intent(this, UnreadArticlesPreferenceActivity.class));
	finish();
    }
}
