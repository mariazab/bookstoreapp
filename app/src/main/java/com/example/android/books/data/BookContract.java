package com.example.android.books.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BookContract {

    private BookContract() {

    }

    //String containing the content authority
    public static final String CONTENT_AUTHORITY = "com.example.android.books";

    //Base content URI created with the CONTENT_AUTHORITY
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Path for books table
    public static final String PATH_BOOKS = "books";

    //Class that defines constant values for books table
    //Each entry represents a single item
    public static abstract class BookEntry implements BaseColumns {

        //Content URI to access the book data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        //The MIME type of CONTENT_URI for a list of books
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //The MIME type of CONTENT_URI for a single book
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        //Constant for Table Name
        public static final String TABLE_NAME = "books";

        //Constants for Column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BOOK_NAME = "book_name";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_NUMBER = "supplier_number";

    }
}

