package com.avans.listurmovies.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.avans.listurmovies.R;
import com.avans.listurmovies.domain.list.MovieList;
import com.avans.listurmovies.domain.list.MovieListResults;
import com.avans.listurmovies.domain.movie.Movie;
import com.avans.listurmovies.domain.user.User;

public class MovieListOverview extends AppCompatActivity {

    private UserViewModel mUserViewModel;
    private MovieListViewModel mMovieListViewModel;
    private int mCurrentPage = 1;
    private int mLastPage = 1;
    private MovieListAdapter adapter;
    private MovieListResults listResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist_overview);

        RecyclerView recyclerView = findViewById(R.id.movielist_recyclerview);
        adapter = new MovieListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mMovieListViewModel = ViewModelProviders.of(this).get(MovieListViewModel.class);
        mUserViewModel.getUser().observe(MovieListOverview.this, user -> {
            if(user == null) return;
            mMovieListViewModel.getLists(mCurrentPage, user).observe(this, listResults -> {
                if(listResults == null) return;
                this.listResults = listResults;
                adapter.setLists(listResults.getResults());
                mLastPage = listResults.getTotal_pages();
                for(MovieList movieList : listResults.getResults()) {
                    Log.d("list", movieList.getId());
                    Log.d("list", movieList.getName());
                    Log.d("list", movieList.getDescription());

                }
            });

        });

        ItemTouchHelper helper = new ItemTouchHelper(new
                                                             ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT
                                                                     , ItemTouchHelper.LEFT) {
                                                                 @Override
                                                                 public boolean onMove(RecyclerView recyclerView,
                                                                                       RecyclerView.ViewHolder viewHolder,
                                                                                       RecyclerView.ViewHolder target) {

                                                                     return false;

                                                                 }

                                                                 @Override
                                                                 public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                                                                      int direction) {
                                                                     MovieList currentList = listResults.getResults().get(viewHolder.getAdapterPosition());
                                                                     //Movie currentMovie = listWithMovies.getItems().get(viewHolder.getAdapterPosition());
                                                                     Log.d("listid", currentList.getId());
                                                                     mMovieListViewModel.deleteList(currentList.getId());
                                                                     listResults.getResults().remove(viewHolder.getAdapterPosition());
                                                                     adapter.notifyItemRemoved(viewHolder.getAdapterPosition());


                                                                 }
                                                             });
        helper.attachToRecyclerView(recyclerView);



    }



    /*private void loadLists(int mCurrentPage, User userinfo){

        mMovieListViewModel.getLists(mCurrentPage, userinfo).observe(this, listResults -> {
            if(listResults == null) return;
            adapter.setLists(listResults.getResults());
            mLastPage = listResults.getTotal_pages();
            for(MovieList movieList : listResults.getResults()) {
                Log.d("list", movieList.getId());
                Log.d("list", movieList.getName());
                Log.d("list", movieList.getDescription());

            }
        });



    }*/


}