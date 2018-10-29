package com.example.amosh.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;

public class UnitProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = UnitProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the units table
     */
    private static final int UNITS = 100;

    /**
     * URI matcher code for the content URI for a single unit in the units table
     */
    private static final int UNIT_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.amosh.inventoryapp/units" will map to the
        // integer code {@link #UNITS}. This URI is used to provide access to MULTIPLE rows
        // of the units table.
        sUriMatcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_UNITS, UNITS);

        // The content URI of the form "content://com.example.amosh.inventoryapp/units/#" will map to the
        // integer code {@link #UNIT_ID}. This URI is used to provide access to ONE single row
        // of the units table.
        
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.amosh.inventoryapp/units/3" matches, but
        // "content://com.example.amosh.inventoryapp/units" (without a number at the end) doesn't match.
        sUriMatcher.addURI(UnitContract.CONTENT_AUTHORITY, UnitContract.PATH_UNITS + "/#", UNIT_ID);
    }
    /**
     * Database helper object
     */
    private UnitDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new UnitDbHelper(getContext());
        return true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case UNITS:
                // For the UNITS code, query the units table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the units table.
                cursor = database.query(UnitEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case UNIT_ID:
                // For the UNIT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.amosh.inventoryapp/units/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = UnitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the units table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(UnitEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //set notification URI on the Cursor,
        //so we know what content URI the Cursor was created for,
        //If the data at this URI changed, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case UNITS:
                return UnitEntry.CONTENT_LIST_TYPE;
            case UNIT_ID:
                return UnitEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case UNITS:
                return insertUnit(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a unit into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertUnit(Uri uri, ContentValues values) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Check that the name is not null
        String name = values.getAsString(UnitEntry.COLUMN_UNIT_NAME);
        if (name != null && name.isEmpty()) {
            throw new IllegalArgumentException("supply requires a name");
        }
        
        //Check the Quantity is not null
        Integer quantity = values.getAsInteger(UnitEntry.COLUMN_UNIT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("supply requires valid quantity");

        }

        //Check the Quantity is not null
        Float price = values.getAsFloat(UnitEntry.COLUMN_UNIT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("please add valid unit price");
        }

        // Insert the new unit with the given values
        long id = database.insert(UnitEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the unit content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case UNITS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(UnitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case UNIT_ID:
                // Delete a single row given by the ID in the URI
                selection = UnitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(UnitEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case UNITS:
                return updateunit(uri, contentValues, selection, selectionArgs);
            case UNIT_ID:
                // For the UNIT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = UnitEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateunit(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update units in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more units).
     * Return the number of rows that were successfully updated.
     */
    private int updateunit(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link unitEntry#COLUMN_UNIT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(UnitEntry.COLUMN_UNIT_NAME)) {
            String name = values.getAsString(UnitEntry.COLUMN_UNIT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("unit requires a name");
            }
        }

        // If the {@link unitEntry#COLUMN_UNIT_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(UnitEntry.COLUMN_UNIT_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(UnitEntry.COLUMN_UNIT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("unit requires valid quantity");
            }
        }

        // check that the price value is valid.
        if (values.containsKey(UnitEntry.COLUMN_UNIT_PRICE)) {
            // Check that the price is greater than or equal to 0 $
            Float price = values.getAsFloat(UnitEntry.COLUMN_UNIT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("please add valid unit price");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(UnitEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }
}

