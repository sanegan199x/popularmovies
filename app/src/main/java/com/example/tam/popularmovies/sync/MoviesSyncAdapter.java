package com.example.tam.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.tam.popularmovies.BuildConfig;
import com.example.tam.popularmovies.R;
import com.example.tam.popularmovies.Utility;
import com.example.tam.popularmovies.data.MoviesContract;
import com.example.tam.popularmovies.model.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;


/**
 * Created by TAM on 1/8/2017.
 */

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;



    public MoviesSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
    }


    /**
     * Get JSON string from the specific uri
     * @param uri
     * @return
     */
    public String getJsonFromUri(Uri uri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String JsonStr = null;  // store JSON string is response

        try {
            // Connect to server
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do
                return null;
            }

            String line;
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            if ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Nothing to read
                return null;
            }

            JsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
            // If the code didn't successfully get the movies data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return JsonStr;
    }


    public Review[] getReviewsFromJsonStr(String JsonStr) throws JSONException{
        // Some constants to get data
        final String OWM_REVIEWS = "results";
        final String OWM_AUTHOR = "author";
        final String OWM_CONTENT = "content";

        JSONObject results = new JSONObject(JsonStr);
        JSONArray reviewObjects = results.getJSONArray(OWM_REVIEWS);

        Review[] reviews = new Review[reviewObjects.length()];

        for (int i = 0; i < reviewObjects.length(); i++) {
            JSONObject reviewObject = reviewObjects.getJSONObject(i);

            Review review = new Review();
            review.setAuthor(reviewObject.getString(OWM_AUTHOR));
            review.setContent(reviewObject.getString(OWM_CONTENT));

            reviews[i] = review;
        }

        return reviews;
    }
    /**
     * Get list of trailer links from the JSON string
     * @param JsonStr
     * @return array of trailer links
     * @throws JSONException
     */
    public String[] getTrailerLinksFromJsonStr(String JsonStr) throws JSONException{
        // Some constants to get data
        final String OWM_VIDEO_LINKS_LIST = "results";
        final String OWM_VIDEO_LINK_KEY = "key";

        // The base url for build URL for Youtube video
        final String BASE_URL = "https://youtube.com/watch";
        final String KEY_PARAM = "v";

        Uri trailerUri = Uri.parse(BASE_URL);

        JSONObject results = new JSONObject(JsonStr);
        JSONArray trailerList = results.getJSONArray(OWM_VIDEO_LINKS_LIST);

        String[] trailerLinks = new String[trailerList.length()];

        for (int i = 0; i < trailerList.length(); i++) {
            JSONObject videoLink = trailerList.getJSONObject(i);
            String key = videoLink.getString(OWM_VIDEO_LINK_KEY);

            // Build trailer link and background of trailer here
            trailerLinks[i] = trailerUri.buildUpon().appendQueryParameter(KEY_PARAM, key).build().toString();
        }
        return trailerLinks;
    }

    /**
     * Get banner of movie from the JSON string
     * @param JsonStr
     * @return the banner link of movie
     * @throws JSONException
     */
    public String getBannerFromJsonStr(String JsonStr) throws JSONException{
        // Some constants to get data
        final String OWM_VIDEO_LINKS_LIST = "results";
        final String OWM_VIDEO_LINK_KEY = "key";

        final String BASE_URL = "https://img.youtube.com/vi";
        final String QUALITY_PARAM = "hqdefault.jpg";

        Uri uri = Uri.parse(BASE_URL);

        JSONObject results = new JSONObject(JsonStr);
        JSONArray trailerList = results.getJSONArray(OWM_VIDEO_LINKS_LIST);
        if (trailerList.length() > 0) {
            JSONObject firstTrailer = trailerList.getJSONObject(0);
            String key = firstTrailer.getString(OWM_VIDEO_LINK_KEY);
            Uri bannerUri = uri.buildUpon()
                    .appendPath(key)
                    .appendPath(QUALITY_PARAM)
                    .build();
            return bannerUri.toString();
        }
        return null;
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        String typeQuery = "popular";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String JsonStr = null;  // store JSON string is response
        String[] result = null;

        // Some constants to build uri for getting data
        final String BASE_URL = "http://api.themoviedb.org/3/movie";
        final String API_PARAM = "api_key";

        // Build a uri identify data
        // There are two uri:
        // http://api.themoviedb.org/3/movie/popular?api_key=[key]
        // http://api.themoviedb.org/3/movie/top_rated?api_key=[key]
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(typeQuery)
                .appendQueryParameter(API_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        try {
//            result = getMoviesFromJsonStr(JsonStr);
            // Connect to server
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do
            }

            String line;
            StringBuffer buffer = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            if ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Nothing to read
                return ;
            }

            JsonStr = buffer.toString();


            // Now parse it and add info to database
            final String OWM_MOVIES = "results";
            final String OWM_ID = "id";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_ORIGIN_TITLE = "original_title";
            final String OWM_TITLE = "title";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_VOTE_AVERAGE = "vote_average";
            final String OWM_POPULARITY = "popularity";

            // Make a JSON Object from JSON string input
            try {
                JSONObject reslults = new JSONObject(JsonStr);
                JSONArray movies = reslults.getJSONArray(OWM_MOVIES);

                // Vector store all movies to bulk insert
                Vector<ContentValues> trailersVector = new Vector<>();
                Vector<ContentValues> reviewsVector = new Vector<>();
                for (int i = 0; i < movies.length(); i++) {
                    JSONObject movieObject = movies.getJSONObject(i);

                    String id = movieObject.getString(OWM_ID);
                    String posterPath = Utility.buildPosterFullPath(movieObject.getString(OWM_POSTER_PATH));
                    String originTitle = movieObject.getString(OWM_ORIGIN_TITLE);
                    String title = movieObject.getString(OWM_TITLE);
                    String overview = movieObject.getString(OWM_OVERVIEW);
                    String releaseDate = movieObject.getString(OWM_RELEASE_DATE);
                    String voteAverage = movieObject.getString(OWM_VOTE_AVERAGE);
                    String popularity = movieObject.getString(OWM_POPULARITY);

                    // Now, we must get banner of movie
                    // We get from uri: https://img.youtube.com/vi/[key]/hqdefault.jpg
                    // But first, get the trailer link from themoviedb.org
                    // To get them, perform query: http://api.themoviedb.org/3/movie/[movie_id]/videos?api_key=[]
                    final String BASE_BANNER_URL = "https://img.youtube.com/vi/";
                    final String VIDES_PARAM = "videos";
                    Uri trailerLinksUri = Uri.parse(BASE_URL).buildUpon()
                            .appendPath(id)
                            .appendPath(VIDES_PARAM)
                            .appendQueryParameter(API_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();

                    String trailerLinksJsonStr = getJsonFromUri(trailerLinksUri);
                    String[] trailerLinks = getTrailerLinksFromJsonStr(trailerLinksJsonStr);
                    String banner = getBannerFromJsonStr(trailerLinksJsonStr);

                    // Get review of movie
                    final String REVIEWS_PARAM = "reviews";
                    Uri reviewsUri = Uri.parse(BASE_URL).buildUpon()
                            .appendPath(id)
                            .appendPath(REVIEWS_PARAM)
                            .appendQueryParameter(API_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                            .build();
                    String reviewsLinksJsonStr = getJsonFromUri(reviewsUri);
                    Review[] reviews = getReviewsFromJsonStr(reviewsLinksJsonStr);

                    // Now, add data into movies table
                    ContentValues movieValue = new ContentValues();
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_ID, id);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, posterPath);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_ORIGIN_TITLE, originTitle);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_BANNER, banner);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, releaseDate);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                    movieValue.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, popularity);

                    // Check if movie with movie_id is exist?
                    Cursor moviesCursor = getContext().getContentResolver().query(
                            MoviesContract.MoviesEntry.CONTENT_URI,
                            new String[]{MoviesContract.MoviesEntry.COLUMN_ID},
                            MoviesContract.MoviesEntry.COLUMN_ID + " = ?",
                            new String[]{id},
                            null
                    );
                    long _id;
                    if (moviesCursor.moveToFirst()) {
                        int _idIndex = moviesCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_ID);
                        _id = moviesCursor.getLong(_idIndex);
                    } else {
                        Uri insertedUri = getContext().getContentResolver().insert(MoviesContract.MoviesEntry.CONTENT_URI, movieValue);
                        _id = ContentUris.parseId(insertedUri);
                    }
                    moviesCursor.close();

                    for (String trailerLink : trailerLinks) {
                        ContentValues contentValues = new ContentValues();

                        contentValues.put(MoviesContract.TrailersEntry.COLUMN_MOVIE_KEY, String.valueOf(_id));
                        contentValues.put(MoviesContract.TrailersEntry.COLUMN_TRAILER, trailerLink);
                        trailersVector.add(contentValues);
                    }
                    if (trailersVector.size() > 0) {
                        int inserted = 0;
                        ContentValues[] cvTrailerValues = new ContentValues[trailersVector.size()];
                        trailersVector.toArray(cvTrailerValues);
                        inserted = getContext().getContentResolver().bulkInsert(MoviesContract.TrailersEntry.CONTENT_URI, cvTrailerValues);
                    }

                    for (Review review : reviews) {
                        ContentValues contentValues = new ContentValues();

                        contentValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIE_KEY, String.valueOf(_id));
                        contentValues.put(MoviesContract.ReviewsEntry.COLUMN_AUTHOR, review.getAuthor());
                        contentValues.put(MoviesContract.ReviewsEntry.COLUMN_CONTENT, review.getContent());
                        reviewsVector.add(contentValues);
                    }

                    if (reviewsVector.size() > 0) {
                        int inserted = 0;
                        ContentValues[] cvReviewValues = new ContentValues[reviewsVector.size()];
                        reviewsVector.toArray(cvReviewValues);
                        inserted = getContext().getContentResolver().bulkInsert(MoviesContract.ReviewsEntry.CONTENT_URI, cvReviewValues);
                    }

                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
        }catch (IOException e) {
            return ;
        }
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
