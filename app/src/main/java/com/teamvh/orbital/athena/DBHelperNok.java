package com.teamvh.orbital.athena;

/**
 * Created by Yang on 22-May-15.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelperNok extends SQLiteOpenHelper {
//
//    // Table Name
//    public static final String TABLE_NAME = "TRACKLIST";
//
//    // Table columns
//    public static final String _ID = "_id";
//    public static final String TRACK_LONG = "Longitude";
//    public static final String TRACK_LAT = "Latitude";
//    public static final String TRACK_ADDR = "Address";
//    public static final String TRACK_TIME = "DateTime";

    // Database Information
    //Table name
    public static final String TABLE_NAME = "NOKTable";

    //label the column names
    public static final String _ID = "nokId";
    public static final String NOK_NAME = "nokName";
    public static final String NOK_EMAIL = "nokEmail";
    public static final String NOK_PHONE = "nokPhone";

    public int nok_ID;
    public String nok_name;
    public String nok_email;
    public int nok_phone;
    //Map db
    static final String DB_NAME = "NOK_DETAILS.DB";

    // database version
    static final int DB_VERSION = 1;

    public DBHelperNok(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Creating table query
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TRACK_ADDR + " TEXT NOT NULL, " + TRACK_LONG + " TEXT NOT NULL, " + TRACK_LAT + " TEXT, "  + TRACK_TIME + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_STUDENT = "CREATE TABLE " + TABLE_NAME  + "("
                + _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + NOK_NAME + " TEXT, "
                + NOK_EMAIL + " TEXT "
                + NOK_PHONE + " INTEGER )";
        db.execSQL(CREATE_TABLE_STUDENT);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}