package com.udacity.popular_movies.detail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popular_movies.BuildConfig;
import com.udacity.popular_movies.Config;
import com.udacity.popular_movies.MovieViewHolder;
import com.udacity.popular_movies.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Reviews;
import info.movito.themoviedbapi.model.Video;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.tv_synopsis)
    TextView tv_synopsis;
    @BindView(R.id.tv_release_date)
    TextView tv_release_date;
    @BindView(R.id.tv_user_rating)
    TextView tv_user_rating;
    @BindView(R.id.list_trailers)
    LinearLayout list_trailers;
    @BindString(R.string.trailer_label)
    String trailer;
    @BindView(R.id.list_reviews)
    LinearLayout list_reviews;
    @BindString(R.string.review_by)
    String review_by;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detail);
            ButterKnife.bind(this);

            Intent intent = getIntent();
            if (intent == null) {
                closeOnError();
            }
            int movieId = intent.getIntExtra(Intent.EXTRA_TEXT, 0);

            new MovieTask().execute(movieId);

        } catch (Exception e) {
            Log.e("DetailActivity", e.getMessage());
            closeOnError();
        }
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, "Error while loading movie data", Toast.LENGTH_SHORT).show();
    }

    private void fillActivity(MovieContainer container, String completePath) {
        try {
            Picasso.with(getApplicationContext())
                    .load(completePath)
                    .into(imageView);
            MovieDb movieDb = container.getMovieDb();
            List<String> trailers = container.getTrailers();
            tv_title.setText(movieDb.getTitle());
            tv_synopsis.setText(movieDb.getOverview());
            tv_release_date.setText(movieDb.getReleaseDate());
            tv_user_rating.setText(String.valueOf(movieDb.getVoteAverage()));
            for (int i = 0; i < trailers.size(); i++){
                createTrailerButton(trailers.get(i), i + 1);
            }
            for (Reviews review : movieDb.getReviews()){
                createReview(review);
            }
        } catch (Exception e) {
            Log.e("DetailActivity", e.getMessage());
            closeOnError();
        }
    }

    private void createTrailerButton(final String trailerKey, int number){
        Button watchTrailerButton = new Button(this);
        watchTrailerButton.setText(trailer + " " + number);
        list_trailers.addView(watchTrailerButton);
        watchTrailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://youtu.be/" + trailerKey));
                getApplicationContext().startActivity(webIntent);
            }
        });
    }

    private void createReview(Reviews review){
        TextView authorView = new TextView(this);
        authorView.setTypeface(null, Typeface.BOLD);
        authorView.setText(review_by + " " +review.getAuthor());
        TextView contentView = new TextView(this);
        contentView.setText(review.getContent());
        list_reviews.addView(authorView);
        list_reviews.addView(contentView);
    }


    class MovieTask extends AsyncTask<Integer, Void, MovieContainer> {

        TmdbApi api;

        @Override
        protected MovieContainer doInBackground(Integer... ids) {
            int id = ids[0];
            api = new TmdbApi(BuildConfig.TMDB_API_KEY);
            MovieDb movieDb = api.getMovies().getMovie(id, Config.getLanguage(), TmdbMovies.MovieMethod.videos, TmdbMovies.MovieMethod.reviews);
            List<String> trailers = new ArrayList<>();
            for (Video v : movieDb.getVideos()) {
                if (v.getType().equals("Trailer") && v.getSite().equals("YouTube")) {
                    trailers.add(v.getKey());
                }
            }
            return new MovieContainer(movieDb, trailers);
        }

        @Override
        protected void onPostExecute(MovieContainer result) {
            String completePath = api.getConfiguration().getBaseUrl() + Config.getHighResolution() + result.getMovieDb().getBackdropPath();
            fillActivity(result, completePath);
        }
    }

    class MovieContainer {
        MovieDb movieDb;
        List<String> trailers;

        public MovieContainer(MovieDb movieDb, List<String> trailers) {
            this.movieDb = movieDb;
            this.trailers = trailers;
        }

        public MovieDb getMovieDb() {
            return movieDb;
        }

        public void setMovieDb(MovieDb movieDb) {
            this.movieDb = movieDb;
        }

        public List<String> getTrailers() {
            return trailers;
        }

        public void setTrailers(List<String> trailers) {
            this.trailers = trailers;
        }
    }
}
