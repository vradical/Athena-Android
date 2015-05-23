//package com.teamvh.orbital.athena;
//
///**
// * Created by YANG on 5/22/2015.
// */
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.SQLException;
//import android.database.sqlite.SQLiteDatabase;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class SQLControlllerNOK  {
//    private DBHelperNok dbHelperNok;
//    private Context ourcontext2;
//    private SQLiteDatabase database;
//
//    public SQLControlllerNOK(Context context) {
//        dbHelperNok = new DBHelperNok(context);
//    }
////    public SQLControlllerNOK(Context c) {
////        ourcontext2 = c;
////    }
//
//    public SQLControlllerNOK open() throws SQLException {
//        dbHelperNok = new DBHelperNok(ourcontext2);
//        database = dbHelperNok.getWritableDatabase();
//        return this;
//    }
//    public int insert(NOKInfo nokInfo){
//        //Open connection to write data
//        SQLiteDatabase db = dbHelperNok.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(dbHelperNok.col_NAME,nokInfo.get_nokname());
//        values.put(DBHelperNok.col_NAME,nokInfo.nok_name);
//        values.put(NOKInfo.NOK_PHONE, nokInfo.nok_phone);
//
//        // Inserting Row
//        long nok_id = db.insert(NOKInfo.TABLE_NAME, null, values);
//        db.close(); // Closing database connection
//        return (int) nok_id;
//    }
//
//    public void delete(int nok_id) {
//
//        SQLiteDatabase db = dbHelperNok.getWritableDatabase();
//        // It's a good practice to use parameter ?, instead of concatenate string
//        db.delete(NOKInfo.TABLE_NAME, NOKInfo._ID + "= ?", new String[] { String.valueOf(nok_id) });
//        db.close(); // Closing database connection
//    }
//
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
//
//}
