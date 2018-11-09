package com.example.amosh.inventoryapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;

public class UnitDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = UnitDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "supply.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link UnitDbHelper}.
     *
     * @param context of the app
     */
    public UnitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the units table
        String SQL_CREATE_UNITS_TABLE =  "CREATE TABLE " + UnitEntry.TABLE_NAME + " ("
                + UnitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UnitEntry.COLUMN_UNIT_NAME+ " TEXT NOT NULL, "
                + UnitEntry.COLUMN_UNIT_PRICE + " INTEGER NOT NULL, "
                + UnitEntry.COLUMN_UNIT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + UnitEntry.COLUMN_UNIT_IMAGE_URI + " TEXT );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_UNITS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    public void buyOne(long id, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        int newQuantity = 0;
        if (quantity > 0) {
            newQuantity = quantity - 1;
        }
        ContentValues values = new ContentValues();
        values.put(UnitEntry.COLUMN_UNIT_QUANTITY, newQuantity);
        String selection = UnitEntry._ID + "=?";
        String[] selectionArgs = new String[]{(String.valueOf(id))};
        db.update(UnitEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public Cursor readStock() {

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {UnitEntry._ID,
                UnitEntry.COLUMN_UNIT_NAME,
                UnitEntry.COLUMN_UNIT_QUANTITY,
                UnitEntry.COLUMN_UNIT_PRICE,
                UnitEntry.COLUMN_UNIT_IMAGE_URI
        };

        Cursor cursor = db.query(UnitEntry.TABLE_NAME, projection,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }
}

