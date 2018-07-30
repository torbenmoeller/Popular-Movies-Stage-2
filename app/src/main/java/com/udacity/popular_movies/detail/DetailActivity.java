package com.udacity.popular_movies.detail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popular_movies.BuildConfig;
import com.udacity.popular_movies.Config;
import com.udacity.popular_movies.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
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
    @BindView(R.id.btn_watch_trailer)
    Button btn_watch_trailer;

    String trailerKey;

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

    private void fillActivity(MovieDb movieDb, String completePath) {
        try {
            Picasso.with(getApplicationContext())
                    .load(completePath)
                    .into(imageView);
            tv_title.setText(movieDb.getTitle());
            tv_synopsis.setText(movieDb.getOverview());
            tv_release_date.setText(movieDb.getReleaseDate());
            tv_user_rating.setText(String.valueOf(movieDb.getVoteAverage()));
            trailerKey = getTrailer(movieDb);
            if(trailerKey == null){
                btn_watch_trailer.setText("Not available");
            }
        } catch (Exception e) {
            Log.e("DetailActivity", e.getMessage());
            closeOnError();
        }
    }

    @OnClick(R.id.btn_watch_trailer)
    public void openYoutube(Button button) {
        if(trailerKey == null){
            return;
        }
//        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://youtu.be/" + trailerKey));
//        try {
            getApplicationContext().startActivity(webIntent);
//        } catch (ActivityNotFoundException ex) {
//            context.startActivity(webIntent);
//        }


    }



    private String getTrailer(MovieDb movieDb){
        for(Video v : movieDb.getVideos()){
            if(v.getType().equals("Trailer") && v.getSite().equals("YouTube")){
                return v.getKey();
            }
        }
        return null;
    }



    class MovieTask extends AsyncTask<Integer, Void, MovieDb> {

        TmdbApi api;

        @Override
        protected MovieDb doInBackground(Integer... ids) {
            int id = ids[0];
            api = new TmdbApi(BuildConfig.TMDB_API_KEY);
            return api.getMovies().getMovie(id, Config.getLanguage(), TmdbMovies.MovieMethod.videos);
        }

        @Override
        protected void onPostExecute(MovieDb result) {
            String completePath = api.getConfiguration().getBaseUrl() + Config.getHighResolution() + result.getBackdropPath();
            fillActivity(result, completePath);
        }
    }
}
