package dev.emmaguy.pocketwidget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import dev.emmaguy.pocketwidget.RetrieveAccessTokenAsyncTask.OnAccessTokenRetrievedListener;
import dev.emmaguy.pocketwidget.RetrieveRequestTokenAsyncTask.OnUrlRetrievedListener;

public class UnreadArticlesPreferenceActivity extends PreferenceActivity implements OnUrlRetrievedListener,
	OnPreferenceClickListener, OnAccessTokenRetrievedListener {

    public static final String SHARED_PREFERENCES = "pocketWidget";

    protected Method loadHeaders = null;
    protected Method hasHeaders = null;

    private static boolean isInitialised = false;
    private static List<Header> headers;
    private static SharedPreferences prefs;
    private int appWidgetId;

    /**
     * Checks to see if using new v11+ way of handling PrefsFragments.
     * 
     * @return Returns false pre-v11, else checks to see if using headers.
     */
    public boolean isNewV11Prefs() {
	if (hasHeaders != null && loadHeaders != null) {
	    try {
		return (Boolean) hasHeaders.invoke(this);
	    } catch (IllegalArgumentException e) {
	    } catch (IllegalAccessException e) {
	    } catch (InvocationTargetException e) {
	    }
	}
	return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle aSavedState) {
	try {
	    loadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class);
	    hasHeaders = getClass().getMethod("hasHeaders");
	} catch (NoSuchMethodException e) {
	}
	
	super.onCreate(aSavedState);
	prefs = getSharedPreferences(SHARED_PREFERENCES, 0);

	if (!isNewV11Prefs()) {
	    addPreferencesFromResource(R.xml.preference_login);

	    PreferenceScreen screen = (PreferenceScreen) findPreference("authentication_preferencescreen");
	    screen.setTitle(getLoginPreferenceScreenTitle());
	    screen.setSummary(getLoginPreferenceScreenSummary());
	    screen.setOnPreferenceClickListener(this);
	}

	updateAccountHeader();

	Bundle extras = getIntent().getExtras();

	if (extras != null) {
	    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

	    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
		prefs.edit().putInt("appWidgetId", appWidgetId).commit();
	    } else {
		appWidgetId = prefs.getInt("appWidgetId", AppWidgetManager.INVALID_APPWIDGET_ID);
	    }
	}

	if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
	    Log.e("UnreadArticlesWidget", "Invalid appWidgetId found");
	    finish();
	}

	String accessToken = prefs.getString("access_token", null);
	if (accessToken != null && accessToken.length() > 0) {
	    refreshWidget(this, appWidgetId);
	    return;
	} else {
	    if (!isInitialised) {
		isInitialised = true;

		final Intent intent2 = getIntent();
		intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, intent2);
		finish();
		startActivity(intent2);
	    }
	}
    }

    @Override
    public void onBuildHeaders(List<Header> headersList) {
	try {
	    headers = headersList;
	    loadHeaders.invoke(this, new Object[] { R.xml.preference_headers, headersList });
	} catch (IllegalArgumentException e) {
	} catch (IllegalAccessException e) {
	} catch (InvocationTargetException e) {
	}
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static public class PrefsFragment extends PreferenceFragment implements OnPreferenceClickListener {
	@Override
	public void onCreate(Bundle aSavedState) {
	    super.onCreate(aSavedState);

	    Context context = getActivity().getApplicationContext();
	    Resources resources = context.getResources();
	    String resourcesValue = getArguments().getString("pref-resource");
	    int thePrefRes = resources.getIdentifier(resourcesValue, "xml", context.getPackageName());
	    addPreferencesFromResource(thePrefRes);

	    PreferenceScreen authPreferencesScreen = (PreferenceScreen) findPreference("authentication_preferencescreen");
	    authPreferencesScreen.setTitle(getLoginPreferenceScreenTitle());
	    authPreferencesScreen.setSummary(getLoginPreferenceScreenSummary());
	    authPreferencesScreen.setOnPreferenceClickListener(this);

	    updateAccountHeader();
	}

	@Override
	public void onResume() {
	    super.onResume();

	    retrieveAccessToken(getActivity());
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
	    beginSignInProcessOrSignOut(preference, getActivity());
	    updateAccountHeader();
	    return true;
	}
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
	beginSignInProcessOrSignOut(preference, this);
	updateAccountHeader();
	return true;
    }

    @Override
    public void onRetrievedUrl(String url) {
	redirectToBrowser(url, this);
    }

    @Override
    public void onResume() {
	super.onResume();

	retrieveAccessToken(this);
    }

    @Override
    public void onRetrievedAccessToken() {
	updateAccountHeader();
	showHomeScreenAndFinish(this);
    }

    
    private static void updateAccountHeader() {
	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	    buildHeader();
	}
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void buildHeader() {
	if (headers != null && headers.size() > 0) {
	    Header account = headers.get(0);
	    final String username = prefs.getString("username", "");
	    if (username != null && username.length() > 0) {
		account.title = "Pocket Account (" + username + ")";
	    } else {
		account.title = "Pocket Account";
	    }
	}
    }

    private static void retrieveAccessToken(Activity activity) {
	Uri uri = activity.getIntent().getData();
	if (uri != null && uri.toString().startsWith("pocketwidget") && !isSignedIn()) {
	    new RetrieveAccessTokenAsyncTask(activity.getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnAccessTokenRetrievedListener) activity, prefs, activity, "Retrieving access token from Pocket")
		    .execute();
	}
    }

    private static void redirectToBrowser(String url, final Activity activity) {
	Toast.makeText(activity, "Redirecting to browser to authenticate with Pocket", Toast.LENGTH_LONG).show();
	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	activity.finish();
	activity.startActivity(intent);
    }

    private static void beginSignInProcessOrSignOut(Preference preference, final Activity activity) {
	if (isSignedIn()) {
	    prefs.edit().clear().commit();
	    Toast.makeText(activity, "Account cleared", Toast.LENGTH_LONG).show();

	    // refresh this activity
	    activity.finish();
	    activity.startActivity(activity.getIntent());
	} else {
	    new RetrieveRequestTokenAsyncTask(activity.getResources().getString(R.string.pocket_consumer_key_mobile),
		    (OnUrlRetrievedListener) activity, prefs, activity, "Retrieving request token from Pocket")
		    .execute();
	}
    }

    private static String getLoginPreferenceScreenTitle() {
	if (isSignedIn()) {
	    final String username = prefs.getString("username", "");
	    return "Sign out " + username;
	}

	return "Sign in with Pocket";
    }

    private static String getLoginPreferenceScreenSummary() {
	if (isSignedIn()) {
	    return "Tap to sign out of your Pocket account";
	}

	return "Tap to sign in to your Pocket account";
    }

    private static boolean isSignedIn() {
	String accessToken = prefs.getString("access_token", null);
	return accessToken != null && accessToken.length() > 0;
    }

    private static void showHomeScreenAndFinish(final Activity activity) {
	Toast.makeText(activity, "Successfully logged in as: " + prefs.getString("username", ""), Toast.LENGTH_LONG)
		.show();

	int appWidgetId = prefs.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
	if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
	    refreshWidget(activity, appWidgetId);
	} else {
	    activity.finish();
	    activity.startActivity(activity.getIntent());
	}
    }

    private static void refreshWidget(final Activity activity, int appWidgetId) {
	new UnreadArticlesWidgetProvider().onUpdate(activity, AppWidgetManager.getInstance(activity), new int[] { appWidgetId });
    }
}