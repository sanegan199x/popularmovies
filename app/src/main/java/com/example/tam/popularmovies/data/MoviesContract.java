package com.example.tam.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by TAM on 1/7/2017.
 */

public class MoviesContract {
    // The "Content Authority" is similar website domain, but in this case is URI
    public static final String CONTENT_AUTHORITY = "attt.bk.hanoi.popularmovies.app";

    // The "Base Content URI" is similar protocol + website domain
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String MOVIE_PATH = "movie";

    public static final String REVIEW_PATH = "review";

    public static final String TRAILER_PATH = "trailer";


    public static class MoviesEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(MOVIE_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIE_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + MOVIE_PATH;

        public static final String TABLE_NAME = "movies";

        // The id is response by themoviedb.org
        public static final String COLUMN_ID = "movie_id";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_ORIGIN_TITLE = "origin_title";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_BANNER = "banner";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_POPULARITY = "popularity";


        // Get movie's id from uri
        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        // Build a uri from CONTENT_URI and movie's id
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Build uri to get popular movies
        public static Uri buildMoviesPopular() {
            String POPULAR_PATH = "popular";
            return CONTENT_URI.buildUpon()
                    .appendPath(POPULAR_PATH)
                    .build();
        }
    }

    public static class ReviewsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(REVIEW_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + REVIEW_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + REVIEW_PATH;

        public static final String TABLE_NAME = "reviews";

        // The foreign key that references to _id column of movies table
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static class TrailersEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TRAILER_PATH).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TRAILER_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TRAILER_PATH;

        public static final String TABLE_NAME = "trailers";

        // The foreign key that references to _id column of movies table
        public static final String COLUMN_MOVIE_KEY = "movie_id";

        public static final String COLUMN_TRAILER = "trailer_link";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
