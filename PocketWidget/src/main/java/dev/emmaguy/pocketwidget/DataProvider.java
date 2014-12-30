package dev.emmaguy.pocketwidget;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.text.SimpleDateFormat;

public class DataProvider extends ContentProvider {
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".DataProvider";
    private static final String SCHEME = "content";
    private static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    public static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm");

    public static final String ID = "_id";
    public static final String DATE = "date";
    public static final String DATE_TICKS = "date_ticks";
    public static final String UNREAD_COUNT = "unread_count";

    private static final int UNREAD_ARTICLES = 1;
    private static final int LATEST_UNREAD = 2;
    private static final int UNREAD_ARTICLES_DATE = 3;

    private static final String ALL_UNREAD_ARTICLES = "unread_articles";
    private static final String LATEST_UNREAD_COUNT = "latest_unread";
    private static final String UNREAD_ARTICLES_BY_DATE = "unread_by_date";

    public static final Uri UNREAD_ARTICLES_COUNT_URI = Uri.withAppendedPath(CONTENT_URI, ALL_UNREAD_ARTICLES);
    public static final Uri UNREAD_ARTICLES_BY_DATE_URI = Uri.withAppendedPath(CONTENT_URI, UNREAD_ARTICLES_BY_DATE);
    public static final Uri LATEST_UNREAD_COUNT_URI = Uri.withAppendedPath(CONTENT_URI, LATEST_UNREAD_COUNT);

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, ALL_UNREAD_ARTICLES, UNREAD_ARTICLES);
        sUriMatcher.addURI(AUTHORITY, UNREAD_ARTICLES_BY_DATE, UNREAD_ARTICLES_DATE);
        sUriMatcher.addURI(AUTHORITY, LATEST_UNREAD_COUNT, LATEST_UNREAD);
    }

    private static final String UNREAD_ARTICLES_TABLE_NAME = "UnreadArticles";
    private UnreadArticlesDatabase mUnreadArticlesDatabase;

    public DataProvider() {
    }

    @Override
    public boolean onCreate() {
        mUnreadArticlesDatabase = new UnreadArticlesDatabase(getContext());

        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mUnreadArticlesDatabase.getWritableDatabase();

        return db.delete(getType(uri), selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return UNREAD_ARTICLES_TABLE_NAME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long insertedId = -1;
        try {
            SQLiteDatabase db = mUnreadArticlesDatabase.getWritableDatabase();
            insertedId = db.insert(UNREAD_ARTICLES_TABLE_NAME, null, values);
        } catch (Exception e) {
            Logger.Log("Exception whilst inserting unread count", e);
        }

        return Uri.withAppendedPath(uri, Long.toString(insertedId));
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case LATEST_UNREAD:
                SQLiteDatabase db = mUnreadArticlesDatabase.getReadableDatabase();
                return db.rawQuery("SELECT " + UNREAD_COUNT + ", " + ID +
                        " FROM " + UNREAD_ARTICLES_TABLE_NAME +
                        " ORDER BY " + DATE_TICKS + " LIMIT 1", null);
            case UNREAD_ARTICLES_DATE:
                SQLiteDatabase db1 = mUnreadArticlesDatabase.getReadableDatabase();
                return db1.rawQuery("SELECT " + DATE + ", " + UNREAD_COUNT +
                        " FROM " + UNREAD_ARTICLES_TABLE_NAME +
                        " GROUP BY strftime('%Y%m%d', " + DATE + ") " +
                        " ORDER BY " + DATE_TICKS, null);
            default:
                return runQuery(uri, columns, selection, selectionArgs, null, sortOrder);
        }
    }

    private Cursor runQuery(Uri uri, String[] columns, String selection, String[] selectionArgs, String groupBy, String sortOrder) {
        SQLiteDatabase db = mUnreadArticlesDatabase.getReadableDatabase();
        return db.query(getType(uri), columns, selection, selectionArgs, groupBy, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mUnreadArticlesDatabase.getWritableDatabase();
        return db.update(getType(uri), values, selection, selectionArgs);
    }

    class UnreadArticlesDatabase extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "PocketWidgetDb";
        private static final int DATABASE_VERSION = 1;

        public UnreadArticlesDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + UNREAD_ARTICLES_TABLE_NAME +
                        " ( " +
                        ID + " INTEGER AUTO INCREMENT, " +
                        DATE + " L(16) PRIMARY KEY NOT NULL," +
                        DATE_TICKS + " INTEGER NOT NULL," +
                        UNREAD_COUNT + " INTEGER NOT NULL" +
                        " );");

            } catch (Exception e) {
                Logger.Log("Failed to create db", e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}