package com.teamvh.orbital.athena;

/**
 * Created by Ron on 18-May-15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.util.Date;

public class SQLController {

    private DBHelper dbHelper;
    private Context ourcontext;
    private SQLiteDatabase database;

    public SQLController(Context c) {
        ourcontext = c;
    }

    public SQLController open() throws SQLException {
        dbHelper = new DBHelper(ourcontext);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(Double longitude, Double latitude, String address) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DBHelper.TRACK_ADDR, address);
        contentValue.put(DBHelper.TRACK_LONG, String.valueOf(longitude));
        contentValue.put(DBHelper.TRACK_LAT, String.valueOf(latitude));
        contentValue.put(DBHelper.TRACK_TIME, String.valueOf(DateFormat.getDateTimeInstance().format(new Date())));

        database.insert(DBHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DBHelper._ID, DBHelper.TRACK_ADDR, DBHelper.TRACK_LONG,
                DBHelper.TRACK_LAT, DBHelper.TRACK_TIME};
        Cursor cursor = database.query(DBHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, Double longitude, Double latitude, String address) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TRACK_ADDR, address);
        contentValues.put(DBHelper.TRACK_LONG, String.valueOf(longitude));
        contentValues.put(DBHelper.TRACK_LAT, String.valueOf(latitude));
        int i = database.update(DBHelper.TABLE_NAME, contentValues,
                DBHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DBHelper.TABLE_NAME, DBHelper._ID + "=" + _id, null);
    }
}
