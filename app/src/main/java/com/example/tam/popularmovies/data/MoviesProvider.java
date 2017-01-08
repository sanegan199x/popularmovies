package com.example.tam.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by TAM on 1/8/2017.
 */

public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    private static final int MOVIE = 100;
    private static final int REVIEW = 200;
    private static final int TRAILER = 300;
    private static final int MOVIE_WITH_ID = 101;
    private static final int MOVIE_POPULAR = 102;
    private static final int MOVIE_TOP_RATED = 103;

    private static final SQLiteQueryBuilder sMoviesReviewsAndTrailersQueryBuilder;

    static{
        sMoviesReviewsAndTrailersQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sMoviesReviewsAndTrailersQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MoviesContract.ReviewsEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry._ID +
                        " = " + MoviesContract.ReviewsEntry.TABLE_NAME +
                        "." + MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY + " INNER JOIN " +
                        MoviesContract.TrailersEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry._ID +
                        " = " + MoviesContract.TrailersEntry.TABLE_NAME +
                        "." + MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = MoviesContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, MoviesContract.MOVIE_PATH, MOVIE);
        uriMatcher.addURI(authority, MoviesContract.REVIEW_PATH, REVIEW);
        uriMatcher.addURI(authority, MoviesContract.TRAILER_PATH, TRAILER);

        uriMatcher.addURI(authority, MoviesContract.MOVIE_PATH + "/#", MOVIE_WITH_ID);
        uriMatcher.addURI(authority, MoviesContract.MOVIE_PATH + "/popular", MOVIE_POPULAR);
        uriMatcher.addURI(authority, MoviesContract.MOVIE_PATH + "/top_rated", MOVIE_TOP_RATED);

        return uriMatcher;
    }


    private Cursor getMovieWithId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MoviesContract.MoviesEntry.getMovieIdFromUri(uri);
        String selection = MoviesContract.MoviesEntry.TABLE_NAME + "." +
                MoviesContract.MoviesEntry.COLUMN_ID + " = ?";
        String[] selectionArgs = new String[]{movieId};
        return  sMoviesReviewsAndTrailersQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getPopularMovies(Uri uri, String[] projection) {
        String sortOrder = MoviesContract.MoviesEntry.COLUMN_POPULARITY + " DESC ";
        String limit = "20";

        return mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                limit);
    }

    private Cursor getTopRatedMovies(Uri uri, String[] projection) {
        String sortOrder = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE + " DESC ";
        String limit = "20";

        return mOpenHelper.getReadableDatabase().query(MoviesContract.MoviesEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                sortOrder,
                limit);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            case TRAILER:
                return MoviesContract.TrailersEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            case MOVIE_POPULAR:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIE_TOP_RATED:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case REVIEW:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRAILER:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.TrailersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case MOVIE_WITH_ID:
                retCursor = getMovieWithId(uri, projection, sortOrder);
                break;
            case MOVIE_POPULAR:
                retCursor = getPopularMovies(uri, projection);
                break;
            case MOVIE_TOP_RATED:
                retCursor = getTopRatedMovies(uri, projection);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MoviesContract.MoviesEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MoviesContract.ReviewsEntry.buildReviewUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case TRAILER: {
                long _id = db.insert(MoviesContract.TrailersEntry.TABLE_NAME, null, contentValues);
                if (_id > 0) {
                    returnUri = MoviesContract.TrailersEntry.buildTrailerUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                rowsDeleted = db.delete(MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(MoviesContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(MoviesContract.TrailersEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MoviesContract.ReviewsEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MoviesContract.TrailersEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                db.beginTransaction();
                int retCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME,
                                null,
                                value);
                        if (_id != -1) {
                            retCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return retCount;
            }
            case REVIEW: {
                db.beginTransaction();
                int retCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME,
                                null,
                                value);
                        if (_id != -1) {
                            retCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return retCount;
            }
            case TRAILER: {
                db.beginTransaction();
                int retCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailersEntry.TABLE_NAME,
                                null,
                                value);
                        if (_id != -1) {
                            retCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return retCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
