package com.example.android.books.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.books.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    //Database helper object
    private BookDbHelper dbHelper;

    //URI matcher code for the content URI for the books table
    private static final int BOOKS = 1;

    //URI matcher code for the content URI for single book in books table
    private static final int BOOK_ID = 2;

    //UriMatcher object
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //All the content URI patterns that the provider should recognize
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    @Override
    public boolean onCreate() {
        //Initialize database helper object to get access to the database
        dbHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //Get readable database
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Cursor for the result of the query
        Cursor cursor = null;

        //Check the match of the URI
        int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                //Query the books table directly with the given parameters
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Query not possible for unknown URI " + uri);
        }

        //Set notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    //Helper method for inserting book into the database based on the given content values.
    private Uri insertBook(Uri uri, ContentValues contentValues) {
        //Check that book name is not null
        String bookName = contentValues.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if(bookName == null || TextUtils.isEmpty(bookName)) {
            throw new IllegalArgumentException("Book requires a title");
        }

        //Check that price is not null, and not negative
        Integer price = contentValues.getAsInteger(BookEntry.COLUMN_PRICE);
        if((price != null && price < 0) || price == null) {
            throw new IllegalArgumentException("Book requires a price");
        }

        //Check that supplier name is not null
        String supplierName = contentValues.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if(supplierName == null || TextUtils.isEmpty(supplierName)) {
            throw new IllegalArgumentException("Book requires a name of supplier");
        }

        //Get writable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Insert the new book with the given values
        long id = db.insert(BookEntry.TABLE_NAME, null, contentValues);

        //Check if the row was inserted successfully
        if (id == -1) {
            //Insertion error
            Log.e("BookProvider", "Failed to insert row");
            return null;
        }

        //Notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);
        //Return the new URI with the ID appended
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //Get writable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Track the number of deleted rows
        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                //Delete a single row by the given ID
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri, null);
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not possible");
        }

        if(rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not possible");
        }
    }

    //Helper method to update a book
    private int updateBook(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        //If book name is updated, check if it is not null
        if(contentValues.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String bookName = contentValues.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if(bookName == null || TextUtils.isEmpty(bookName)) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        //If price is updated, check if it is not null or negative
        if(contentValues.containsKey(BookEntry.COLUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(BookEntry.COLUMN_PRICE);
            if((price != null && price < 0) || price == null) {
                throw new IllegalArgumentException("Book requires a valid price");
            }
        }

        //If supplier name is updated, check if it is not null
        if(contentValues.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = contentValues.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if(supplierName == null || TextUtils.isEmpty(supplierName)) {
                throw new IllegalArgumentException("Book requires supplier name");
            }
        }

        //Get writable database
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Update and get the number of rows affected
        int rowsAffected = db.update(BookEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        //Notify of change if 1 or more rows were updated
        if(rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsAffected;
    }
}
