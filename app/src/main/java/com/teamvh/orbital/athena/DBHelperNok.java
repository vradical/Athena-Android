package com.teamvh.orbital.athena;

/**
 * Created by Yang on 22-May-15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelperNok extends SQLiteOpenHelper {

    static final String DB_NAME = "NOK_DETAILS.DB";

    // database version
    static final int DB_VERSION = 1;
    //table name
    public static final String TABLE_NAME = "NOKTable";

    //label the column names
    public static final String col_ID = "nokId";
    public static final String col_NAME = "nokName";
    public static final String col_EMAIL = "nokEmail";
    public static final String col_PHONE = "nokPhone";


    public DBHelperNok(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public DBHelperNok(Context context, String name,
                       SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_NOK= "CREATE TABLE " + TABLE_NAME  + "( "
                + col_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + col_NAME + " TEXT, "
                + col_EMAIL + " TEXT, "
                + col_PHONE + " INTEGER " + " )";
        db.execSQL(CREATE_TABLE_NOK);

        String sql = "insert into " + TABLE_NAME + " ( " + col_ID + "," + col_NAME + "," + col_EMAIL + "," + col_PHONE + " ) "
                + " values ( '1' ,'C01', 'w0shiyang@hotmail.com','12345678' )";
        db.execSQL(sql);

        String sql2 = "insert into " + TABLE_NAME + " ( " + col_ID + "," + col_NAME + "," + col_EMAIL + "," + col_PHONE + " ) "
                + " values ( '2' ,'C02', 'yangjing.yj56@gmail.com','96733834' )";
        db.execSQL(sql2);

        String sql3 = "insert into " + TABLE_NAME + " ( " + col_ID + "," + col_NAME + "," + col_EMAIL + "," + col_PHONE + " ) "
                + " values ( '3' ,'C03', 'w0shiyang@hotmail.com','12345678' )";
        db.execSQL(sql3);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    //this is for seeDB
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }

    public Cursor fetchAllNOK(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.query(TABLE_NAME, new String[] {},null,null,null,null,null);

        if(mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}