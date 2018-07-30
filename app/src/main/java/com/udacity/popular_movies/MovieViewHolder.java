package com.udacity.popular_movies;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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


    MovieViewHolder(Context context, View itemView, ListItemClickListener mOnClickListener) {
        super(itemView);
        this.view = itemView.findViewById(R.id.image_item);
        this.mOnClickListener = mOnClickListener;
        this.context = context;
    }

    void bind(int position, SortOrder sortOrder ) {
        int pageCount = position / PAGE_LENGTH + 1 ; //Which page to load, starts at offset 1
        int pageOffset = position % PAGE_LENGTH; //Move on page
        new MovieResultPageTask(pageCount, pageOffset, sortOrder).execute();
    }

    private void fillViewHolder(MovieDb movieDb, String completePath){
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
        private final int pageCount;
        private final int pageOffset;
        private final SortOrder sortOrder;

        MovieResultPageTask(int pageCount, int pageOffset, SortOrder sortOrder) {
            this.pageCount = pageCount;
            this.pageOffset = pageOffset;
            this.sortOrder = sortOrder;
        }

        @Override
        protected MovieDb doInBackground(Void... params) {
            api = new TmdbApi(BuildConfig.TMDB_API_KEY);
            MovieResultsPage page;
            switch (sortOrder) {
                case RatingDescending:
                    page =  api.getMovies().getTopRatedMovies(Config.getLanguage(), pageCount);
                    break;
                case PopularityDescending:
                default: //default, if error happened
                    page =  api.getMovies().getPopularMovies(Config.getLanguage(), pageCount);
                    break;
            }
            return page.getResults().get(pageOffset);
        }

        @Override
        protected void onPostExecute(MovieDb movieDb) {
            String path = movieDb.getPosterPath();
            String completePath = api.getConfiguration().getBaseUrl() + Config.getResolution() + path;
            fillViewHolder(movieDb, completePath);
        }
    }
}