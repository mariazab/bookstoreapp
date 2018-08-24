package com.example.android.books;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.books.data.BookContract;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //Current item's id
        int idColumnIndex = cursor.getColumnIndex(BookContract.BookEntry._ID);
        final long id = cursor.getLong(idColumnIndex);

        //Find the book name text view and set the value from cursor
        TextView textViewBookName = view.findViewById(R.id.book_name);
        int bookNameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_NAME);
        String bookName = cursor.getString(bookNameColumnIndex);
        textViewBookName.setText(bookName);

        //Find the author text view and set the value from cursor
        TextView textViewAuthor = view.findViewById(R.id.author);
        int authorColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_AUTHOR);
        String author = cursor.getString(authorColumnIndex);
        if(TextUtils.isEmpty(author)) {
            author = context.getString(R.string.unknown_author);
        }
        textViewAuthor.setText(author);

        //Find the price text view and set the value from cursor
        TextView textViewPrice = view.findViewById(R.id.price);
        int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_PRICE);
        int price = cursor.getInt(priceColumnIndex);
        textViewPrice.setText(Integer.toString(price));

        //Find the quantity text view and set the value from cursor
        TextView textViewQuantity = view.findViewById(R.id.quantity);
        int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_QUANTITY);
        final int quantity = cursor.getInt(quantityColumnIndex);
        textViewQuantity.setText(Integer.toString(quantity));

        //Set onClickListener on "Sale" button
        Button saleBtn = view.findViewById(R.id.sale_btn);
        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);
                sale(currentUri, quantity, context);
            }
        });
    }

    //Helper method to decrease quantity by one when "Sale" button is clicked
    private void sale(Uri currentBookUri, int quantity, Context context) {
        //If quantity is equal to 0, return early to avoid negative values
        if(quantity == 0) {
            return;
        }
        //Decrease quantity by one
        int newQuantity = quantity - 1;
        //Create content values with the new quantity
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, newQuantity);
        //Update the database
        int rowsAffected = context.getContentResolver().update(currentBookUri, values, null, null);
    }
}
