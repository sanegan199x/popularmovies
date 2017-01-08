package com.example.tam.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by TAM on 1/8/2017.
 */

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testMoviesTable() {
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.MoviesEntry._ID, 1);
        testValues.put(MoviesContract.MoviesEntry.COLUMN_ID, 1234);
        testValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, "AQFAfdawT");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_ORIGIN_TITLE, "The secret of life");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, "The secret of life");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_BANNER, "banner");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, "overview");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, "2016");
        testValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, 5.6);
        testValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, 123.4);

        long moviesId = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, testValues);

        assert (moviesId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MoviesEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        validateCurrentRecord("Error: Location Query Validation Failed", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        cursor.close();
        db.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
