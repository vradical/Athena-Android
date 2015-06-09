package com.teamvh.orbital.athena;

/**
 * Created by YANG on 5/22/2015.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLControlllerNOK  {
    private DBHelperNok dbHelperNok;
    private Context ourcontext;
    private SQLiteDatabase database;

    public SQLControlllerNOK(Context c) {
        ourcontext = c;
    }

    public SQLControlllerNOK open() throws SQLException {
        dbHelperNok = new DBHelperNok(ourcontext);
        database = dbHelperNok.getWritableDatabase();
        return this;
    }

    public SQLControlllerNOK opnToRead() {
        dbHelperNok = new DBHelperNok(ourcontext,
                dbHelperNok.DB_NAME, null, dbHelperNok.DB_VERSION);
        database = dbHelperNok.getReadableDatabase();
        return this;
    }

    public SQLControlllerNOK opnToWrite() {
        dbHelperNok = new DBHelperNok(ourcontext,
                dbHelperNok.DB_NAME, null, dbHelperNok.DB_VERSION);
        database = dbHelperNok.getWritableDatabase();
        return this;
    }

    public void Close() {
        database.close();
    }


    public long insert(NOKInfo nokInfo){
        //Open connection to write data
        ContentValues values = new ContentValues();
        values.put(dbHelperNok.col_NAME,nokInfo.get_nokname());
        values.put(dbHelperNok.col_EMAIL,nokInfo.get_nokemail());
        values.put(dbHelperNok.col_PHONE, nokInfo.get_nokphone());
        // Inserting Row
        return database.insert(dbHelperNok.TABLE_NAME, null, values);
    }

    public Cursor fetchAllNOK(){

        String[] cols = {
                DBHelperNok.col_NAME, DBHelperNok.col_EMAIL, DBHelperNok.col_PHONE};
        //Cursor mCursor = database.query(DBHelperNok.TABLE_NAME, cols, null, null, null, null, null);
        Cursor mCursor = database.rawQuery("SELECT " +  DBHelperNok.col_ID + " AS _id " + ","  + DBHelperNok.col_NAME + " , " + DBHelperNok.col_EMAIL
                                             + " , "  + DBHelperNok.col_PHONE + " FROM " + DBHelperNok.TABLE_NAME, null);
        opnToWrite();
        if(mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    public void delete(int nok_id) {

        SQLiteDatabase db = dbHelperNok.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(dbHelperNok.TABLE_NAME, dbHelperNok.col_ID + "= ?", new String[]{String.valueOf(nok_id)});
        db.close(); // Closing database connection
    }

    public int getNumOfNOK(){
        String countQuery = "SELECT  * FROM " + DBHelperNok.TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public String[]getNOKPhone(){
        String countQuery = "SELECT " + DBHelperNok.col_PHONE  +  " FROM " + DBHelperNok.TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        String[] data = new String[cursor.getCount()];
        if (cursor != null) {
            cursor.moveToNext();
            int i = 0;
            while (i < cursor.getCount()) {
                int j = 0;
                data[i] = cursor.getString(i);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return data;
    }

    public String[] getNOKEmail(){
        String countQuery = "SELECT " + DBHelperNok.col_EMAIL  +  " FROM " + DBHelperNok.TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        String[] data = new String[cursor.getCount()];
        if (cursor != null) {
            cursor.moveToNext();
            int i = 0;
            while (i < cursor.getCount()) {
                int j = 0;
                data[i] = cursor.getString(i);
                i++;
                cursor.moveToNext();
            }
            cursor.close();
        }
        return data;
    }
////    public Cursor fetch() {
////        String[] columns = new String[] { DBHelperNok._ID, DBHelperNok.NOK_NAME, DBHelperNok.NOK_EMAIL,
////                DBHelperNok.NOK_PHONE};
////        Cursor cursor = database.query(DBHelperNok.TABLE_NAME, columns, null, null, null, null, null);
////        if (cursor != null) {
////            cursor.moveToFirst();
////        }
////        return cursor;
////    }
//
//    public void update(NOKInfo nokInfo) {
//
//        SQLiteDatabase db = dbHelperNok.getWritableDatabase();
//        ContentValues values = new ContentValues();
//
//        values.put(NOKInfo.NOK_EMAIL,nokInfo.nok_email);
//        values.put(NOKInfo.NOK_NAME,nokInfo.nok_name);
//        values.put(NOKInfo.NOK_PHONE, nokInfo.nok_phone);
//
//        // It's a good practice to use parameter ?, instead of concatenate string
//        db.update(NOKInfo.TABLE_NAME, values, NOKInfo._ID + "= ?", new String[]{String.valueOf(nokInfo.nok_ID)});
//        db.close(); // Closing database connection
//    }
//
//    public ArrayList<HashMap<String, String>>  getNOKList() {
//        //Open connection to read only
//        SQLiteDatabase db = dbHelperNok.getReadableDatabase();
//        String selectQuery =  "SELECT  " +
//                NOKInfo._ID + "," +
//                NOKInfo.NOK_EMAIL + "," +
//                NOKInfo.NOK_NAME + "," +
//                NOKInfo.NOK_PHONE +
//                " FROM " + NOKInfo.TABLE_NAME;
//
//        ArrayList<HashMap<String, String>> nokList = new ArrayList<HashMap<String, String>>();
//
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do {
//                HashMap<String, String> nokHM = new HashMap<String, String>();
//                nokHM.put("id", cursor.getString(cursor.getColumnIndex(NOKInfo._ID)));
//                nokHM.put("name", cursor.getString(cursor.getColumnIndex(NOKInfo.NOK_NAME)));
//                nokList.add(nokHM);
//
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//        return nokList;
//    }
//
//    public NOKInfo getNOKByID(int Id){
//        SQLiteDatabase db = dbHelperNok.getReadableDatabase();
//        String selectQuery =  "SELECT  " +
//                NOKInfo._ID + "," +
//                NOKInfo.NOK_NAME + "," +
//                NOKInfo.NOK_EMAIL + "," +
//                NOKInfo.NOK_PHONE +
//                " FROM " + NOKInfo.TABLE_NAME
//                + " WHERE " +
//                NOKInfo._ID + "=?";
//
//        int iCount =0;
//        NOKInfo nokinfo = new NOKInfo();
//        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );
//
//        if (cursor.moveToFirst()) {
//            do {
//                nokinfo.nok_ID =cursor.getInt(cursor.getColumnIndex(NOKInfo._ID));
//                nokinfo.nok_name =cursor.getString(cursor.getColumnIndex(NOKInfo.NOK_NAME));
//                nokinfo.nok_email  =cursor.getString(cursor.getColumnIndex(NOKInfo.NOK_EMAIL));
//                nokinfo.nok_phone =cursor.getInt(cursor.getColumnIndex(NOKInfo.NOK_PHONE));
//            } while (cursor.moveToNext());
//        }
//
//        cursor.close();
//        db.close();
//        return nokinfo;
//    }
//

}
