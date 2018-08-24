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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.books.data.BookContract.BookEntry;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //URI for the current book
    private Uri currentBookUri;

    //EditText for book title
    private EditText editTextBookTitle;

    //EditText for author
    private EditText editTextAuthor;

    //EditText for price
    private EditText editTextPrice;

    //TextView for quantity
    private TextView textViewQuantity;

    //EditText for supplier name
    private EditText editTextSupplierName;

    //EditText for supplier phone number
    private EditText editTextSupplierNumber;

    //Default quantity
    private int quantity = 1;

    private static final int EDIT_BOOK_LOADER = 1;

    //Boolean to track if the book has been edited or not
    private boolean bookHasChanged = false;

    //OnTouchListeners that listens for any touches on a View
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editTextBookTitle = findViewById(R.id.edit_book_title);
        editTextAuthor = findViewById(R.id.edit_author);
        editTextPrice = findViewById(R.id.edit_price);
        textViewQuantity = findViewById(R.id.edit_quantity);
        editTextSupplierName = findViewById(R.id.edit_supplier_name);
        editTextSupplierNumber = findViewById(R.id.edit_supplier_number);

        //Set default value to the text view quantity
        textViewQuantity.setText(String.valueOf(quantity));

        //Check if what intent launched this activity
        // in order to differentiate between Edit and Add screens
        Intent intent = getIntent();
        currentBookUri = intent.getData();

        if(currentBookUri == null) {
            setTitle(getString(R.string.action_add_item));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_title));
            getLoaderManager().initLoader(EDIT_BOOK_LOADER, null, this);
        }

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

        //Set onClickListener on "Save" button
        final Button saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean saveSuccessful = saveBook();
                //if the save was successful, finish the activity
                //otherwise stay in the activity so that the user can still edit the item
                if (saveSuccessful) {
                    finish();
                }
            }
        });

        //Set OnTouchListeners on all input fields, to determine if the user has
        //touched or modified them
        editTextBookTitle.setOnTouchListener(touchListener);
        editTextAuthor.setOnTouchListener(touchListener);
        editTextPrice.setOnTouchListener(touchListener);
        plusBtn.setOnTouchListener(touchListener);
        minusBtn.setOnTouchListener(touchListener);
        editTextSupplierName.setOnTouchListener(touchListener);
        editTextSupplierNumber.setOnTouchListener(touchListener);
    }

    //Helper method to save book from user input
    private boolean saveBook() {
        //boolean to store the info if the book was successfully saved or not
        boolean isSaveSuccessful = false;

        //Read from input fields and delete whitespace
        String bookName = editTextBookTitle.getText().toString().trim();
        String author = editTextAuthor.getText().toString().trim();
        String priceString = editTextPrice.getText().toString().trim();
        String quantityString = textViewQuantity.getText().toString().trim();
        String supplierName = editTextSupplierName.getText().toString().trim();
        String supplierNumber = editTextSupplierNumber.getText().toString().trim();

        int quantity = Integer.parseInt(quantityString);

        //If all the fields are empty and quantity is 1(which is default value), this means the user did not edit anything and nothing can be saved
        // so just finish the activity
        if(currentBookUri == null && TextUtils.isEmpty(bookName) && TextUtils.isEmpty(author) && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierName) && TextUtils.isEmpty(supplierNumber) && quantity == 1) {
            isSaveSuccessful = true;
            return isSaveSuccessful;
        }

        //Add the values from fields to content values
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, bookName);
        values.put(BookEntry.COLUMN_AUTHOR, author);
        //if the price is not provided, set the default price 0
        int price = 0;
        if(!TextUtils.isEmpty(priceString)) {
            try {
                price = Integer.parseInt(priceString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter valid price", Toast.LENGTH_LONG).show();
                isSaveSuccessful = false;
                return isSaveSuccessful;
            }
        }
        values.put(BookEntry.COLUMN_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_NUMBER, supplierNumber);

        //If the current uri is null, new book is created
        if(currentBookUri == null) {
            try {
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
                //Show toast message if the insert was successful or not
                if (newUri == null) {
                    isSaveSuccessful = false;
                    //Error
                    Toast.makeText(this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
                } else {
                    isSaveSuccessful = true;
                    //Success
                    Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException ex) {
                //Catch the exception thrown by the provider, which means some value was null
                //Show the error message in toast
                isSaveSuccessful = false;
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                int rowsUpdated = getContentResolver().update(currentBookUri, values, null, null);
                //Show toast message if update was successful or not
                if (rowsUpdated == 0) {
                    //Error
                    isSaveSuccessful = false;
                    Toast.makeText(this, "Update unsuccessful", Toast.LENGTH_SHORT).show();
                } else {
                    //Success
                    isSaveSuccessful = true;
                    Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException ex) {
                //Catch the exception thrown by the provider, which means some value was null
                //Show the error message in toast
                isSaveSuccessful = false;
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return isSaveSuccessful;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //If the book hasn't changed, continue with navigating back
                if(!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                //If there are unsaved changes, setup warning dialog
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Clicked "Discard"
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                };
                //Show dialog that notifies of unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Dialog that shows the user warning dialog of unsaved changes
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //"Keep Editing" clicked, dismiss the dialog
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        //Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        //If the book hasn't changed, continue with back button
        if(!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        //If there are unsaved changes, setup a warning dialog
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //"Discard" clicked, close the activity
                finish();
            }
        };
        //Show the dialog about unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //Method to increase quantity by 1
    private void increaseQuantity() {
        int quantity = Integer.parseInt(textViewQuantity.getText().toString());
        int newQuantity = quantity + 1;
        textViewQuantity.setText(String.valueOf(newQuantity));

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
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);

            //Set the values on the TextViews
            editTextBookTitle.setText(bookTitle);
            editTextAuthor.setText(author);
            editTextPrice.setText(Integer.toString(price));
            textViewQuantity.setText(Integer.toString(quantity));
            editTextSupplierName.setText(supplierName);
            editTextSupplierNumber.setText(supplierNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        editTextBookTitle.setText("");
        editTextAuthor.setText("");
        editTextPrice.setText("");
        textViewQuantity.setText("");
        editTextSupplierName.setText("");
        editTextSupplierNumber.setText("");
    }
}
