package dev.emmaguy.pocketwidget;

import android.util.Log;

public class Logger {
    public static void Log(String message) {
        Log.d("PocketWidget", message);
    }

    public static void Log(String message, Throwable throwable) {
        Log.e("PocketWidget", message, throwable);
    }
}
