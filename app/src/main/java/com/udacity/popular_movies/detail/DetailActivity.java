package com.udacity.popular_movies.detail;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.udacity.popular_movies.R;
import com.udacity.popular_movies.data.FavoriteContract;
import com.udacity.popular_movies.data.FavoriteEntry;

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
    @BindView(R.id.btn_favorit)
    Button btn_favorit;

    int movie_id;
    String movie_title;


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

            movie_id = movieDb.getId();
            movie_title = movieDb.getTitle();

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
            Long favId = getFavoritId();
            if(favId == null){
                setBtnFavoritOff();
            } else{
                setBtnFavoritOn();
            }
        } catch (Exception e) {
            Log.e("DetailActivity", e.getMessage());
            closeOnError();
        }
    }

    //Source stackoverflow: https://stackoverflow.com/questions/12523005/how-set-background-drawable-programmatically-in-android
    void setBtnFavoritOn(){
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.btn_star_big_on);
        btn_favorit.setBackground(drawable);
    }

    void setBtnFavoritOff(){
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.btn_star_big_off);
        btn_favorit.setBackground(drawable);
    }

    @OnClick(R.id.btn_favorit)
    void favoritButtonClick(View view) {
        Long id = getFavoritId();
        if(id == null) {
            addFavorit();
            setBtnFavoritOn();
        }else{
            removeFavorit(id);
            setBtnFavoritOff();
        }
    }

    void addFavorit(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoriteEntry.COLUMN_MOVIE_ID, movie_id);
        contentValues.put(FavoriteEntry.COLUMN_MOVIE_TITLE, movie_title == null? "" : movie_title);
        getContentResolver().insert(FavoriteContract.CONTENT_URI, contentValues);
    }

    Long getFavoritId(){
        Cursor cursor = getContentResolver().query(
                FavoriteContract.CONTENT_URI,
                null,
                FavoriteEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movie_id)},
                FavoriteEntry.COLUMN_MOVIE_ID
        );
        try {
            if (cursor != null && cursor.moveToFirst()) { //Thanks to StackOverflow: https://stackoverflow.com/questions/10244222/android-database-cursorindexoutofboundsexception-index-0-requested-with-a-size
                Long fav = cursor.getLong(cursor.getColumnIndex("_id"));
                Log.d("DB-query", "Favorit found " + fav);
                return fav;
            } else {
                Log.d("DB-query", "Favorit not found ");
                return null;
            }
        }
        finally {
            cursor.close();
        }
    }

    void removeFavorit(Long id){
        Uri uri = FavoriteContract.CONTENT_URI.buildUpon().appendPath(id.toString()).build();
        getContentResolver().delete(uri,null,null);
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
