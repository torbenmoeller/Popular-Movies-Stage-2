package com.udacity.popular_movies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder>{

    private final ListItemClickListener mOnClickListener;
    private final SortOrder sortOrder;

    @Override
    public int getItemCount() {
        return 100;//page.getTotalResults();
    }

    MovieAdapter(ListItemClickListener listener, SortOrder sortOrder) {
        this.mOnClickListener = listener;
        this.sortOrder = sortOrder;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieViewHolder(context, view, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
            holder.bind(position, sortOrder);
    }
}
