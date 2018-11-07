package com.example.amosh.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;
import com.example.amosh.inventoryapp.data.UnitDbHelper;


public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the unit data loader */
    private static final int UNIT_LOADER = 0;

    /** Adapter for the ListView */
    UnitCursorAdapter mCursorAdapter;
    UnitDbHelper mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mDbHelper = new UnitDbHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
       
        // Find the ListView which will be populated with the unit data
        ListView unitListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        unitListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of unit data in the Cursor.
        // There is no unit data yet (until the loader finishes) so pass in null for the Cursor.
        Cursor cursor = mDbHelper.readStock();
        mCursorAdapter = new UnitCursorAdapter(this, cursor);
        unitListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        unitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                final Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific unit that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link unitEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.amosh.inventoryapp/units/2"
                // if the unit with ID 2 was clicked on.
                final Uri currentUnitUri = ContentUris.withAppendedId(UnitEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentUnitUri);

                // Launch the {@link EditorActivity} to display the data for the current unit.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(UNIT_LOADER, null, this);
    }
    /**
     * Helper method to insert hardcoded unit data into the database. For debugging purposes only.
     */
    private void insertUnit() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's unit attributes are the values.
        ContentValues values = new ContentValues();
        values.put(UnitEntry.COLUMN_UNIT_NAME, "Cheese");
        values.put(UnitEntry.COLUMN_UNIT_QUANTITY, 5);
        values.put(UnitEntry.COLUMN_UNIT_PRICE, 7);
        values.put(UnitEntry.COLUMN_UNIT_IMAGE_URI, "android.resource://com.example.amosh.inventoryapp/drawable/shopping_empty_box_icon");

        // Insert a new row for Cheese into the provider using the ContentResolver.
        // Use the {@link UnitEntry#CONTENT_URI} to indicate that we want to insert
        // into the units database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(UnitEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all units in the database.
     */
    private void deleteAllUnits() {
        int rowsDeleted = getContentResolver().delete(UnitEntry.CONTENT_URI, null, null);
        Log.v("InventoryActivity", rowsDeleted + " rows deleted from unit database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertUnit();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllUnits();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                UnitEntry._ID,
                UnitEntry.COLUMN_UNIT_NAME,
                UnitEntry.COLUMN_UNIT_QUANTITY,
                UnitEntry.COLUMN_UNIT_PRICE,
                UnitEntry.COLUMN_UNIT_IMAGE_URI};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                UnitEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link unitCursorAdapter} with this new cursor containing updated unit data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    public void buyOne(long id, int quantity) {
        mDbHelper.buyOne(id, quantity);
        mCursorAdapter.swapCursor(mDbHelper.readStock());

    }
}