package dev.emmaguy.pocketwidget.ui;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import timber.log.Timber;

public class SettingsActivity extends AppCompatActivity {

    public static final String SHARED_PREFERENCES_NAME = "pocketWidget";
    public static final String EXTRA_KEY_FROM_LAUNCHER = "from_launcher";

    public static final String POCKET_USERNAME = "username";
    public static final String POCKET_AUTH_ACCESS_TOKEN = "accesstoken";
    public static final String POCKET_AUTH_CODE = "code";

    public static final String DASHCLOCK_PACKAGE_NAME = "net.nurik.roman.dashclock";
    public static final String DASHCLOCK_CLASS_NAME_CONFIGURATION_ACTIVITY
            = "com.google.android.apps.dashclock.configuration.ConfigurationActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Timber.d("onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Timber.d("onAuthStateChanged:signed_out");
                }
            }
        };

        firebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override public void onComplete(@NonNull Task<AuthResult> task) {
                Timber.d("signInAnonymously:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Timber.d("signInAnonymously: " + task.getException());
                    Toast.makeText(SettingsActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        boolean showHomeAsUp = true;

        // if we've been opened by the launcher activity, it was from a widget so don't show home as up
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean fromLauncher = extras.getBoolean(EXTRA_KEY_FROM_LAUNCHER);
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                showHomeAsUp = false;
            }

            if (fromLauncher) {
                showHomeAsUp = false;
            }
        }

        // otherwise, likely opened by DashClock, so we want to show back arrow
        if (showHomeAsUp) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    @Override public void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authListener);
    }

    @Override public void onStop() {
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }

        super.onStop();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(DASHCLOCK_PACKAGE_NAME,
                            DASHCLOCK_CLASS_NAME_CONFIGURATION_ACTIVITY));
                    NavUtils.navigateUpTo(this, intent);
                } catch (Exception ignored) {
                    // can't explicitly find/launch DashClock to go back to, so just close self
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}