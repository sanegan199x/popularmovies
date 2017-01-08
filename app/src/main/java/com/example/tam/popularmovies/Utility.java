package com.example.tam.popularmovies;

import android.net.Uri;

/**
 * Created by TAM on 1/8/2017.
 */

public class Utility {
    public static String buildPosterFullPath(String posterPath) {

        final String BASE_URL = "http://image.tmdb.org/t/p/";
        final String SIZE_PARAM = "w185";

        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(SIZE_PARAM)
                .appendPath(posterPath.replace("/", ""))
                .build();

        return uri.toString();
    }

    public static String getFriendlyDate(String date) {
        return null;
    }
}
