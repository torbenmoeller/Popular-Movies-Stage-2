package com.udacity.popular_movies.data;

import android.provider.BaseColumns;

public final class FavoriteEntry implements BaseColumns {

    public static final String TABLE_NAME = "favorites";

    public static final String COLUMN_MOVIE_ID = "movieId";
    public static final String COLUMN_MOVIE_TITLE = "movieTitle";

}