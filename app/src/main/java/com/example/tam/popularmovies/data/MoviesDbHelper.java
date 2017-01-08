package com.example.tam.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by TAM on 1/7/2017.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    // The database name that will be stored physically
    public static final String DATABASE_NAME = "movies.db";

    public static int VERSION_NUMBER = 2;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesContract.MoviesEntry.TABLE_NAME + " (" +
                MoviesContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesContract.MoviesEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_ORIGIN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_BANNER + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL," +
                MoviesContract.MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL" +
                ");";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MoviesContract.ReviewsEntry.TABLE_NAME + " (" +
                MoviesContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MoviesContract.ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME + " (" + MoviesContract.MoviesEntry._ID + ")" +
                ");";

        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + MoviesContract.TrailersEntry.TABLE_NAME + " (" +
                MoviesContract.TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + " INTEGER NOT NULL, " +
                MoviesContract.TrailersEntry.COLUMN_TRAILER + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                MoviesContract.MoviesEntry.TABLE_NAME + " (" + MoviesContract.MoviesEntry._ID + ")" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Its upgrade policy is to simply to discard the data and start over
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.ReviewsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.TrailersEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
