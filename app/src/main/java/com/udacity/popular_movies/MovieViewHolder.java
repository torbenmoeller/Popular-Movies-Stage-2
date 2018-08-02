package com.udacity.popular_movies;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.popular_movies.data.FavoriteContract;
import com.udacity.popular_movies.data.FavoriteEntry;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class MovieViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    private MovieDb movie;
    private ListItemClickListener mOnClickListener = null;
    private final View view;
    private final Context context;
    private final int PAGE_LENGTH = 20; //entries per movieResultPage
    private ContentResolver contentResolver;

    MovieViewHolder(Context context, View itemView, ListItemClickListener mOnClickListener, ContentResolver contentResolver) {
        super(itemView);
        this.view = itemView.findViewById(R.id.image_item);
        this.mOnClickListener = mOnClickListener;
        this.context = context;
        this.contentResolver = contentResolver;
    }

    void bind(int position, SortOrder sortOrder) {
        new MovieResultPageTask(position, sortOrder, contentResolver).execute();
    }

    private void fillViewHolder(MovieDb movieDb, String completePath) {
        ImageView imageView = view.findViewById(R.id.image_item);
        imageView.setOnClickListener(this);
        this.movie = movieDb;
        Picasso.with(context)
                .load(completePath)
                .into(imageView);
        view.refreshDrawableState();
    }

    @Override
    public void onClick(View v) {
        mOnClickListener.onListItemClick(movie.getId());
    }

    class MovieResultPageTask extends AsyncTask<Void, Void, MovieDb> {

        private TmdbApi api;
        private final int position;
        private final int pageCount;
        private final int pageOffset;
        private final SortOrder sortOrder;
        private ContentResolver contentResolver;

        MovieResultPageTask(int position, SortOrder sortOrder, ContentResolver contentResolver) {
            this.position = position;
            this.pageCount = position / PAGE_LENGTH + 1; //Which page to load, starts at offset 1
            this.pageOffset = position % PAGE_LENGTH; //Move on page
            this.sortOrder = sortOrder;
            this.contentResolver = contentResolver;
        }

        @Override
        protected MovieDb doInBackground(Void... params) {
            api = new TmdbApi(BuildConfig.TMDB_API_KEY);
            MovieResultsPage page;
            switch (sortOrder) {
                case Favorite:
                    List<Integer> favorites = getFavorites();
                    if (position >= favorites.size()) {
                        return null; //Invalid size
                    }
                    int movieId = favorites.get(position);
                    return getMovieByID(movieId);
                case RatingDescending:
                    page = api.getMovies().getTopRatedMovies(Config.getLanguage(), pageCount);
                    return page.getResults().get(pageOffset);
                case PopularityDescending:
                default: //default, if error happened
                    page = api.getMovies().getPopularMovies(Config.getLanguage(), pageCount);
                    return page.getResults().get(pageOffset);
            }
        }

        @Override
        protected void onPostExecute(MovieDb movieDb) {
            if (movieDb != null) {
                String path = movieDb.getPosterPath();
                String completePath = api.getConfiguration().getBaseUrl() + Config.getResolution() + path;
                fillViewHolder(movieDb, completePath);
            }
        }


        List<Integer> getFavorites() {
            Cursor cursor = contentResolver.query(
                    FavoriteContract.CONTENT_URI,
                    null,
                    null,
                    null,
                    FavoriteEntry.COLUMN_MOVIE_ID
            );
            try {
                if (cursor != null && cursor.moveToFirst()) { //Thanks to StackOverflow: https://stackoverflow.com/questions/10244222/android-database-cursorindexoutofboundsexception-index-0-requested-with-a-size
                    List<Integer> favoritesIds = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        Integer id = cursor.getInt(cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_ID));
                        favoritesIds.add(id);
                    }
                    return favoritesIds;
                } else {
                    return null;
                }
            } finally {
                cursor.close();
            }
        }

        protected MovieDb getMovieByID(int movieId) {
            return api.getMovies().getMovie(movieId, Config.getLanguage());
        }
    }
}