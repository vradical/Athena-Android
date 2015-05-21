package com.teamvh.orbital.athena;

/**
 * Created by Ron on 18-May-15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String TABLE_NAME = "TRACKLIST";

    // Table columns
    public static final String _ID = "_id";
    public static final String TRACK_LONG = "Longitude";
    public static final String TRACK_LAT = "Latitude";
    public static final String TRACK_ADDR = "Address";
    public static final String TRACK_TIME = "DateTime";

    // Database Information
    static final String DB_NAME = "MAP_TRACKLIST.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TRACK_ADDR + " TEXT NOT NULL, " + TRACK_LONG + " TEXT NOT NULL, " + TRACK_LAT + " TEXT, "  + TRACK_TIME + " TEXT)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}