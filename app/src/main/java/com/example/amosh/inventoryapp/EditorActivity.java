package com.example.amosh.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;
import com.example.amosh.inventoryapp.data.UnitDbHelper;

import java.net.URI;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the unit data loader
     */
    private static final int EXISTING_UNIT_LOADER = 0;

    private static final int PICK_IMAGE_REQUEST = 0;


    /**
     * Content URI for the existing unit (null if it's a new unit)
     */
    private Uri mCurrentUnitUri;

    /**
     * EditText field to enter the units's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the units's breed
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the unit's weight
     */
    private EditText mPriceEditText;

    private final static int PICK_IMAGE = 100;
    Uri imageUri;
    /**
     * Button to add Image from Gallery
     */
    private Button mAddImageButton;
    private String mImageUriString;

    private ImageView mAddedImageView;
    /**
     * Boolean flag that keeps track of whether the unit has been edited (true) or not (false)
     */
    private boolean mUnitHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the munitHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mUnitHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new unit or editing an existing one.
        Intent intent = getIntent();
        mCurrentUnitUri = intent.getData();

        UnitDbHelper dbHelper = new UnitDbHelper(this);


        // If the Intent DOES NOT contain a unit content URI, then we know that we are
        // creating a new unit.
        if (mCurrentUnitUri == null) {
            // This is a new unit, so change the app bar to say "Add a unit"
            setTitle(R.string.editor_activity_title_add_new_unit);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a unit that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing unit, so change the app bar to say "Edit unit"
            setTitle(R.string.editor_activity_title_edit_unit);

            // Initialize a loader to read the unit data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_UNIT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_supply_name);
        mQuantityEditText = findViewById(R.id.edit_supply_quntity);
        mPriceEditText = findViewById(R.id.edit_supply_price);
        mAddImageButton = findViewById(R.id.add_image);
        mAddedImageView = findViewById(R.id.added_image);
        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mAddedImageView.setOnTouchListener(mTouchListener);

    }

    private void openGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                imageUri = resultData.getData();
                mAddedImageView.setImageURI(imageUri);
                mAddedImageView.invalidate();
                mImageUriString = imageUri.toString();
            }

        }
    }

    /**
     * Get user input from editor and save new unit into database.
     */
    private void saveUnit() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String imageUriString = mImageUriString;


        // Check if this is supposed to be a new unit
        // and check if all the fields in the editor are blank
        if (mCurrentUnitUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(imageUriString)) {
            // Since no fields were modified, we can return early without creating a new unit.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and unit attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(UnitEntry.COLUMN_UNIT_NAME, nameString);
        values.put(UnitEntry.COLUMN_UNIT_QUANTITY, quantityString);
        values.put(UnitEntry.COLUMN_UNIT_PRICE, priceString);
        values.put(UnitEntry.COLUMN_UNIT_IMAGE_URI, imageUriString);
        /// Determine if this is a new or existing unit by checking if mCurrentUnitUri is null or not
        if (mCurrentUnitUri == null) {
            // This is a NEW unit, so insert a new unit into the provider,
            // returning the content URI for the new unit.
            Uri newUri = getContentResolver().insert(UnitEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_unit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_unit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING unit, so update the unit with content URI: mCurrentUnitUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentUnitUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentUnitUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_unit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_unit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new unit, hide the "Delete" menu item.
        if (mCurrentUnitUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                String name = mNameEditText.getText().toString().trim();
                String price = mPriceEditText.getText().toString().trim();
                String quantity = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(price)) {
                    Toast.makeText(this, R.string.editor_activity_toaste_plz_add_all, Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    // Save unit to database
                    saveUnit();
                    // Exit activity
                    finish();

                    return true;
                }
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the unit hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mUnitHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the unit hasn't changed, continue with handling back button press
        if (!mUnitHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all unit attributes, define a projection that contains
        // all columns from the unit table
        String[] projection = {
                UnitEntry._ID,
                UnitEntry.COLUMN_UNIT_NAME,
                UnitEntry.COLUMN_UNIT_QUANTITY,
                UnitEntry.COLUMN_UNIT_PRICE,
                UnitEntry.COLUMN_UNIT_IMAGE_URI};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUnitUri,         // Query the content URI for the current unit
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of unit attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_PRICE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Double.toString(price));

            mAddedImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_IMAGE_URI))));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mAddedImageView.setImageURI(Uri.parse(""));
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the unit.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this unit.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the unit.
                deleteUnit();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the unit.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the unit in the database.
     */
    private void deleteUnit() {
        // Only perform the delete if this is an existing unit.
        if (mCurrentUnitUri != null) {
            // Call the ContentResolver to delete the unit at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentUnitUri
            // content URI already identifies the unit that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentUnitUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_unit_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_unit_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}