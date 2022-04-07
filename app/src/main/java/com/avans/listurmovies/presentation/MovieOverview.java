package com.avans.listurmovies.presentation;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;

import com.avans.listurmovies.R;
import com.avans.listurmovies.dataacess.UserRepository;
import com.avans.listurmovies.domain.genre.Genre;
import com.avans.listurmovies.domain.genre.GenreResults;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MovieOverview extends AppCompatActivity {
    private UserRepository mUserRepository;
    private UserViewModel mUserViewModel;
    private MovieViewModel mMovieViewModel;
    private int mCurrentPage = 1;
    private int mLastPage = 1;
    private MovieAdapter adapter;
    private int sort = R.id.popular_movies;
    private DrawerLayout drawer;

    private String mQuery = "";
    private int minRating;
    private int maxRating;

    private Boolean mLoadMovies = true;
    private Boolean mGenreFilter = false;
    private Boolean mRatingFilter = false;
    private Boolean mSearchMovies = false;

    RecyclerView mRecyclerView;

    private List<Genre> genres = new ArrayList<>();
    private List<String> filteredGenres = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mRecyclerView = findViewById(R.id.movie_recyclerview);
        adapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mUserRepository = new UserRepository(this);
        mMovieViewModel = ViewModelProviders.of(this).get(MovieViewModel.class);


        final Observer<GenreResults> genreObserver = new Observer<GenreResults>() {
            @Override
            public void onChanged(GenreResults genreResults) {
                genres.addAll(genreResults.getResult());
            }
        };
        mMovieViewModel.getGenres().observe(MovieOverview.this, genreObserver);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView menu_username = header.findViewById(R.id.menu_username);
        ImageView menu_user_image = header.findViewById(R.id.menu_user_image);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.logout) {
                    mUserRepository.logOut();
                }

                if (id == R.id.movie_lists) {
                    //Log.d("user", mUserViewModel.getUser().getValue().getId());
                    if(mUserViewModel.getUser().getValue().getId() != 0) {
                        Intent intent = new Intent(getApplicationContext(), MovieListOverview.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MovieOverview.this, "Please login first", Toast.LENGTH_LONG).show();
                    }
                }
                if (id == R.id.add_movie_list) {
                    if(mUserViewModel.getUser().getValue().getId() != 0) {
                        Intent intent = new Intent(getApplicationContext(), MovieListAdd.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MovieOverview.this, "Please login first", Toast.LENGTH_LONG).show();
                    }
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //Load user information into the menu
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mUserViewModel.getUser().observe(MovieOverview.this, user -> {
            if(user == null) return;
            //Set the username in the menu bar to the current logged in user
            menu_username.setText(user.getUsername());
            //Set the user image in the menu bar to the current logged in user
            Glide.with(this).load(this.getString(R.string.userImageURL) + user.getAvatarHash()).into(menu_user_image);
        });
        //adapter.setGenres(genres);
        //Load the default movies page
        loadMovies();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        SearchManager searchManager = (SearchManager) getSystemService(this.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filteredGenres.clear();
                mCurrentPage = 1;
                Log.d("submit", "onQueryTextSubmit: " + query);
                mQuery = query;
                loadSearchMovies();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.d("change", "onQueryTextChange: " + query);
                return false;
            }


        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mQuery = "";
                mCurrentPage = 1;
                loadMovies();
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //Set page to 1
        mCurrentPage = 1;

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.filter_genre || id == R.id.filter_rating) {
            showAlertBox(id);
        }

        switch (id) {
            case R.id.now_playing:
                sort = R.id.now_playing;
                loadMovies();
                break;
            case R.id.popular_movies:
                sort = R.id.popular_movies;
                loadMovies();
                break;
            case R.id.top_rated:
                sort = R.id.top_rated;
                loadMovies();
                break;
            case R.id.upcoming:
                sort = R.id.upcoming;
                loadMovies();
                break;
            case R.id.movie_lists:
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAlertBox(int id){
        if(id == R.id.filter_genre){

            List<String> genreNames = new ArrayList<>();
            for(Genre g : genres){
                genreNames.add(g.getName());
            }
            String[] genreArray = genreNames.toArray(new String[genreNames.size()]);
            filteredGenres.clear();


            AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Filter")
            .setMultiChoiceItems(genreArray, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean checked) {
                    if(checked){
                        for(Genre g : genres){
                            if(g.getName().equals(genreArray[position])){
                                filteredGenres.add(g.getId() + "");
                            }
                        }
                    }
                }
            })
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    setGenreFilter();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .show();

        }else if(id == R.id.filter_rating){
            final NumberPicker maxVal = new NumberPicker(this);
            maxVal.setMaxValue(10);
            maxVal.setMinValue(0);

            final NumberPicker minVal = new NumberPicker(this);
            minVal.setMaxValue(10);
            minVal.setMinValue(0);

            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(minVal);
            linearLayout.addView(maxVal);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Filter")
                    .setMessage("Enter a minimum and maximum rating value")
                    .setView(linearLayout)
                    .setPositiveButton(android.R.string.yes, null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();

            Button positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(minVal.getValue() > maxVal.getValue()) return;
                    dialog.dismiss();
                    minRating = minVal.getValue();
                    maxRating = maxVal.getValue();
                    setRatingFilter();
                }
            });
        }
    }

    private void setInActive(){
        mLoadMovies = false;
        mGenreFilter = false;
        mRatingFilter = false;
        mSearchMovies = false;
    }

    private void loadMovies(){
        setInActive();
        mLoadMovies = true;
        mMovieViewModel.getMovies(mCurrentPage, sort).observe(this, movieResults -> {
            if(movieResults == null) return;
            adapter.setMovies(movieResults.getResult());
            mLastPage = movieResults.getTotal_pages();
        });
    }

    private void loadSearchMovies(){
        setInActive();
        mSearchMovies = true;
        mMovieViewModel.searchMovies(mQuery, mCurrentPage).observe(MovieOverview.this, movieResults -> {
            if(movieResults == null) return;
            adapter.setMovies(movieResults.getResult());
            mLastPage = movieResults.getTotal_pages();
        });
    }

    private void getGenres(){
//        mMovieViewModel.getGenres().observe(MovieOverview.this, genreResults -> {
//            genres.addAll(genreResults.getResult());
//        });
    }

    private void setGenreFilter(){
        setInActive();
        mGenreFilter = true;
        String filters = String.join(",", filteredGenres);
        mMovieViewModel.setGenreFilter(filters, mCurrentPage).observe(MovieOverview.this, movieResults -> {
            if(movieResults == null) return;
            adapter.setMovies(movieResults.getResult());
            mLastPage = movieResults.getTotal_pages();
        });
    }

    private void setRatingFilter(){
        setInActive();
        mRatingFilter = true;
        mMovieViewModel.setRatingFilter(minRating, maxRating, mCurrentPage).observe(MovieOverview.this, movieResults -> {
            if(movieResults == null) return;
            adapter.setMovies(movieResults.getResult());
            mLastPage = movieResults.getTotal_pages();
        });
    }

    public void nextMovies(View view) {
        if(mCurrentPage < mLastPage) {
            mCurrentPage++;
            if(mGenreFilter) {
                setGenreFilter();
            }else if(mLoadMovies){
                loadMovies();
            }else if(mSearchMovies){
                loadSearchMovies();
            }else if(mRatingFilter){
                setRatingFilter();
            }

            mRecyclerView.scrollTo(0, mRecyclerView.getTop());
            Toast.makeText(this, "Current page: " + mCurrentPage, Toast.LENGTH_SHORT).show();
        }
    }

    public void previousMovies(View view) {
        if(mCurrentPage == 1) return;
        mCurrentPage--;
        if(mGenreFilter) {
            setGenreFilter();
        }else if(mLoadMovies){
            loadMovies();
        }else if(mSearchMovies){
            loadSearchMovies();
        }else if(mRatingFilter){
            setRatingFilter();
        }

        mRecyclerView.scrollTo(0, mRecyclerView.getTop());
        Toast.makeText(this, "Current page: " + mCurrentPage, Toast.LENGTH_SHORT).show();
    }
}