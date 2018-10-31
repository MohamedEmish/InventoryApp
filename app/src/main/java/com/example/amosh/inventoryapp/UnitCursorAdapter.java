package com.example.amosh.inventoryapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;
import com.example.amosh.inventoryapp.data.UnitDbHelper;
import com.example.amosh.inventoryapp.data.UnitProvider;
import com.squareup.picasso.Picasso;


/**
 * {@link UnitCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of unit data as its data source. This adapter knows
 * how to create list items for each row of unit data in the {@link Cursor}.
 */

public class UnitCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link UnitCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public UnitCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
    }

    /**
     * This method binds the unit data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current unit can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView cartImageView = (ImageView) view.findViewById(R.id.buy_one);
        ImageView unitImageView = (ImageView) view.findViewById(R.id.unit_image_view);

        // Find the columns of unit attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_PRICE);
        final long idCoulmnIndex = cursor.getColumnIndex(UnitEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_IMAGE_URI);

        // Read the unit attributes from the Cursor for the current unit
        String unitName = cursor.getString(nameColumnIndex);
        final String unitQuantity = cursor.getString(quantityColumnIndex);
        String unitPrice = cursor.getString(priceColumnIndex);
        String imageUriString = cursor.getString(imageColumnIndex);

        // Update the TextViews with the attributes for the current unit
        nameTextView.setText(unitName);
        quantityTextView.setText(unitQuantity);
        priceTextView.setText(unitPrice);

        Uri imageUri = Uri.parse(imageUriString);
        Picasso.with(context).load(imageUri).into(unitImageView);
        unitImageView.setImageURI(imageUri);


        cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryActivity inventoryActivity = (InventoryActivity) context;
                inventoryActivity.buyOne(idCoulmnIndex, Integer.valueOf(unitQuantity));
            }
        });
    }
}