package com.example.amosh.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.amosh.inventoryapp.data.UnitContract.UnitEntry;

public class UnitCursorAdapter extends CursorAdapter {


    private final InventoryActivity activity;

    public UnitCursorAdapter(InventoryActivity context, Cursor c) {
        super(context, c, 0);
        this.activity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.item_list, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView buyOne = (ImageView) view.findViewById(R.id.buy_one);
        ImageView image = (ImageView) view.findViewById(R.id.unit_image_view);

        String name = cursor.getString(cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_NAME));
        final int quantity = cursor.getInt(cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_PRICE));

        image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(UnitEntry.COLUMN_UNIT_IMAGE_URI))));

        nameTextView.setText(name);
        quantityTextView.setText(String.valueOf(quantity));
        priceTextView.setText(price);

        final long id = cursor.getLong(cursor.getColumnIndex(UnitEntry._ID));


        buyOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.buyOne(id,
                        quantity);
            }
        });
    }
}
