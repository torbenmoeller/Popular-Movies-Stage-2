package com.udacity.popular_movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.udacity.popular_movies.detail.DetailActivity;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class MainActivity extends AppCompatActivity implements ListItemClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.spinner)
    Spinner spinner;

    @BindString(R.string.most_popular)
    String most_popular;
    @BindString(R.string.highest_rating)
    String highest_rating;
    @BindString(R.string.favorites)
    String favorites;

    Unbinder unbinder;

    private MovieAdapter mAdapter;
    private SortOrder sortOrder = SortOrder.PopularityDescending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setHasFixedSize(true);
        mAdapter = new MovieAdapter(this, sortOrder, getContentResolver());
        recycler_view.setAdapter(mAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) spinner.getSelectedItem();
                if(selectedItem.equals(most_popular)){
                    sortOrder = SortOrder.PopularityDescending;
                }
                else if(selectedItem.equals(highest_rating)){
                    sortOrder = SortOrder.RatingDescending;
                }
                else {
                    sortOrder = SortOrder.Favorite;
                }
                mAdapter = new MovieAdapter(MainActivity.this, sortOrder, getContentResolver());
                recycler_view.invalidate();
                recycler_view.setAdapter(mAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //change nothing, sort order is fine.
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onListItemClick(int movieId) {
        Intent startChildActivityIntent = new Intent(MainActivity.this, DetailActivity.class);
        startChildActivityIntent.putExtra(Intent.EXTRA_TEXT, movieId);
        startActivity(startChildActivityIntent);
    }
}