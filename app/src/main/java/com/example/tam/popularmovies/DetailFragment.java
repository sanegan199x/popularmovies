package com.example.tam.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tam.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by TAM on 1/8/2017.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int DETAIL_LOADER = 0;

    public static String[] DETAIL_COLUMNS = {
            MoviesContract.MoviesEntry.TABLE_NAME + "." + MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_BANNER,
            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesContract.MoviesEntry.COLUMN_TITLE,
            MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
            MoviesContract.ReviewsEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewsEntry.COLUMN_CONTENT,
            MoviesContract.TrailersEntry.COLUMN_TRAILER
    };

    static final int COL_ID = 0;
    static final int COL_BANNER  = 1;
    static final int COL_POSTER_PATH = 2;
    static final int COL_TITLE = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_VOTE_AVERAGE = 5;
    static final int COL_OVERVIEW = 6;
    static final int COL_AUTHOR = 7;
    static final int COL_CONTENT = 8;
    static final int COL_TRAILER = 9;

    private ImageView banner;
    private ImageView avatar;
    private TextView title;
    private TextView releaseDate;
    private TextView rating;
    private TextView overview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detail_fragment, container, false);

        banner = (ImageView)rootView.findViewById(R.id.banner);
        avatar = (ImageView)rootView.findViewById(R.id.avatar);
        title = (TextView)rootView.findViewById(R.id.title);
        releaseDate = (TextView)rootView.findViewById(R.id.release_date);
        rating = (TextView)rootView.findViewById(R.id.average_rating);
        overview = (TextView)rootView.findViewById(R.id.overview);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Intent intent = getActivity().getIntent();
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            // Set banner
            Picasso.with(getActivity()).load(cursor.getString(COL_BANNER))
                    .into(banner);

            // Set avatar
            Picasso.with(getActivity()).load(cursor.getString(COL_POSTER_PATH))
                    .into(avatar);

            // Set title
            title.setText(cursor.getString(COL_TITLE));

            // Set date
            releaseDate.setText(cursor.getString(COL_RELEASE_DATE));;

            // Set rating
            rating.setText(cursor.getString(COL_VOTE_AVERAGE));

            // Set overview
            overview.setText(cursor.getString(COL_OVERVIEW));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
