package com.example.android.books;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.books.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 1;

    private BookCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.list);

        //Find and set empty view on the listView
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        //Setup cursor adapter
        cursorAdapter = new BookCursorAdapter(this, null);
        //Attach it to listView
        listView.setAdapter(cursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                //Create the content URI of the item that was clicked
                Uri uri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            //Respond to a click on the "Add a Book" menu option
            case R.id.action_add_item:
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_data:
                insertData();
                return true;
            // Respond to a click on the "Delete all" menu option
            case R.id.action_delete_all:
                showConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Helper method to insert dummy data when the option is clicked in the menu
    private void insertData() {
        //Create values to be inserted
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, "Fangirl");
        values.put(BookEntry.COLUMN_AUTHOR, "Rainbow Rowell");
        values.put(BookEntry.COLUMN_PRICE, 40);
        values.put(BookEntry.COLUMN_QUANTITY, 15);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "SuperNova");
        values.put(BookEntry.COLUMN_SUPPLIER_NUMBER, "789654321");

        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

    //Helper method to delete all items when "Delete All" option in the menu is clicked
    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Toast.makeText(this, "Deleted " + rowsDeleted + " rows", Toast.LENGTH_SHORT).show();
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
                deleteAll();
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Perform SQL query "SELECT book_name, author, price, quantity, supplier_name FROM books"
        String[] projection = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_AUTHOR, BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_SUPPLIER_NUMBER};

        return new CursorLoader(this, BookEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
