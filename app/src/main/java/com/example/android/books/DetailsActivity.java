package com.example.android.books;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.books.data.BookContract;
import com.example.android.books.data.BookContract.BookEntry;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //URI for the current book
    private Uri currentBookUri;

    //TextView for book title
    private TextView textViewBookTitle;

    //TextView for author
    private TextView textViewAuthor;

    //TextView for price
    private TextView textViewPrice;

    //TextView for quantity
    private TextView textViewQuantity;

    //TextView for supplier name
    private TextView textViewSupplierName;

    //TextView for supplier phone number
    private TextView textViewSupplierNumber;

    //Global string in order to store supplier number
    private String supplierNumber;

    private static final int DETAILS_BOOK_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Save the URI from the Intent that launched this activity to currentBookUri
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        //Find the corresponding text views in order to populate them with the data
        textViewBookTitle = findViewById(R.id.details_book_title);
        textViewAuthor = findViewById(R.id.details_author);
        textViewPrice = findViewById(R.id.details_price);
        textViewQuantity = findViewById(R.id.details_quantity);
        textViewSupplierName = findViewById(R.id.details_supplier_name);
        textViewSupplierNumber = findViewById(R.id.details_supplier_number);

        //Set onClickListener on "Update" button
        final Button updateBtn = findViewById(R.id.update_btn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateIntent = new Intent(DetailsActivity.this, EditActivity.class);
                updateIntent.setData(currentBookUri);
                startActivity(updateIntent);
            }
        });

        //Set onClickListener on "+" button
        Button minusBtn = findViewById(R.id.edit_decrease_quantity);
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseQuantity();
            }
        });

        //Set onClickListener on "+" button
        Button plusBtn = findViewById(R.id.edit_increase_quantity);
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantity();
            }
        });

        //Set onClickListener on "Order" button
        Button orderBtn = findViewById(R.id.order_btn);
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Parse the number to URI and create the intent
                Uri number = Uri.parse("tel:" + supplierNumber);
                Intent orderIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(orderIntent);
            }
        });

        getLoaderManager().initLoader(DETAILS_BOOK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            //Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Helper method to delete one book from the database
    private void deleteBook() {
        int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);
        //Show toast message if the delete was successful
        if(rowsDeleted == 0) {
            Toast.makeText(this, "Delete unsuccessful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Book deleted", Toast.LENGTH_SHORT).show();
        }
        //Finish the activity to return to main
        finish();
    }

    //Method to show confirmation dialog when DELETE is clicked
    private void showConfirmationDialog() {
        //Create an AlertDialog, set message and respond to answers
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //YES clicked, so delete item
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //CANCEL clicked, so dismiss the dialog
                if(dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        //Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Method to increase quantity by 1
    private void increaseQuantity() {
        int quantity = Integer.parseInt(textViewQuantity.getText().toString());
        int newQuantity = quantity + 1;
        textViewQuantity.setText(String.valueOf(newQuantity));
        //Create content values with the new quantity
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, newQuantity);
        //Update the database
        int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);

    }

    //Method to decrease quantity by 1
    private void decreaseQuantity() {
        int quantity = Integer.parseInt(textViewQuantity.getText().toString());
        //Check if the quantity is not already 0
        if(quantity == 0) {
            return;
        }
        int newQuantity = quantity - 1;
        textViewQuantity.setText(String.valueOf(newQuantity));
        //Create content values with the new quantity
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_QUANTITY, newQuantity);
        //Update the database
        int rowsAffected = getContentResolver().update(currentBookUri, values, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Query that contains all the columns for this specific item
        String[] projection = {BookEntry._ID,
                    BookEntry.COLUMN_BOOK_NAME,
                    BookEntry.COLUMN_AUTHOR,
                    BookEntry.COLUMN_PRICE,
                    BookEntry.COLUMN_QUANTITY,
                    BookEntry.COLUMN_SUPPLIER_NAME,
                    BookEntry.COLUMN_SUPPLIER_NUMBER};
        return new CursorLoader(this, currentBookUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Return early if the cursor is null or contains less than 1 row
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        //Move the cursor to the first row
        if(cursor.moveToFirst()) {
            //Get the index of columns
            int bookTitleColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_AUTHOR);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NUMBER);

            //Extract the values from columns
            String bookTitle = cursor.getString(bookTitleColumnIndex);
            String author = cursor.getString(authorColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            supplierNumber = cursor.getString(supplierNumberColumnIndex);

            //If author is empty, set text to "unknown author"
            if(TextUtils.isEmpty(author)) {
                author = getString(R.string.unknown_author);
            }

            //If supplier number is empty, set the text to "No number available"
            if(TextUtils.isEmpty(supplierNumber)) {
                supplierNumber = getString(R.string.unknown_number);
            }

            //Set the values on the TextViews
            textViewBookTitle.setText(bookTitle);
            textViewAuthor.setText(author);
            textViewPrice.setText(Integer.toString(price));
            textViewQuantity.setText(Integer.toString(quantity));
            textViewSupplierName.setText(supplierName);
            textViewSupplierNumber.setText(supplierNumber);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        textViewBookTitle.setText("");
        textViewAuthor.setText("");
        textViewPrice.setText("");
        textViewQuantity.setText("");
        textViewSupplierName.setText("");
        textViewSupplierNumber.setText("");
    }
}
