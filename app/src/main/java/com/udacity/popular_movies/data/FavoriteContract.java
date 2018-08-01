package com.udacity.popular_movies.data;

import android.net.Uri;

public class FavoriteContract {

    public static final String AUTHORITY = "com.udacity.popular_movies.favorit";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

}
