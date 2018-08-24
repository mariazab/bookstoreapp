package com.example.android.books.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.books.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "Bookstore.db";

    public BookDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        //String that contains the SQL statement that creates the books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
                BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, " +
                BookEntry.COLUMN_AUTHOR + " TEXT, " +
                BookEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
                BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                BookEntry.COLUMN_SUPPLIER_NUMBER + " TEXT);";

        Log.v("Book helper", "Create statement:\n" + SQL_CREATE_BOOKS_TABLE);

        database.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE books");
        onCreate(sqLiteDatabase);
    }
}
