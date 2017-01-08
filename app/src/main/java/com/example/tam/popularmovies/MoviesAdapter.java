package com.example.tam.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by TAM on 1/8/2017.
 */

public class MoviesAdapter extends CursorAdapter {
    public static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    private Context mContext;

    public static class ViewHolder {
        public final TextView title;
        public final ImageView poster;
        public ViewHolder(View view) {
            title = (TextView)view.findViewById(R.id.movie_title);
            poster = (ImageView)view.findViewById(R.id.poster);
        }
    }

    public MoviesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext= context;
    }

    public String convertCursorRowToTitleView(Cursor cursor) {
        return cursor.getString(MoviesFragment.COL_TITLE);
    }

    public void convertCursorRowToPosterView(Cursor cursor, ImageView poster) {
        Picasso.with(mContext).load(cursor.getString(MoviesFragment.COL_POSTER_PATH))
                .resize(100, 200)
                .centerCrop()
                .into(poster);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        TextView tv = (TextView)view.findViewById(R.id.movie_title);
//        tv.setText(convertCursorRowToTitleView(cursor));
//
//        ImageView poster = (ImageView)view.findViewById(R.id.poster);
//        convertCursorRowToPosterView(cursor, poster);
        ViewHolder viewHolder = (ViewHolder)view.getTag();

    }
}
