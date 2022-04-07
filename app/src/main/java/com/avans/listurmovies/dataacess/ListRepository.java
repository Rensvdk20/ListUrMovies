package com.avans.listurmovies.dataacess;

import static com.avans.listurmovies.dataacess.UserRepository.SESSION_ID;
import static com.avans.listurmovies.dataacess.UserRepository.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.avans.listurmovies.R;
import com.avans.listurmovies.dataacess.retrofit.MovieAPI;
import com.avans.listurmovies.dataacess.retrofit.RetrofitClient;
import com.avans.listurmovies.dataacess.room.MovieDAO;
import com.avans.listurmovies.dataacess.room.MovieRoomDatabase;
import com.avans.listurmovies.domain.list.MovieData;
import com.avans.listurmovies.domain.list.MovieList;
import com.avans.listurmovies.domain.list.MovieListData;
import com.avans.listurmovies.domain.list.MovieListResults;
import com.avans.listurmovies.domain.movie.MovieResults;
import com.avans.listurmovies.domain.user.User;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListRepository {

    private final MovieAPI mService;
    private Context mContext;
    private MovieDAO mMovieDAO;
    private UserRepository mUserRepository = UserRepository.getInstance();

    private final MutableLiveData<MovieListResults> listOfLists = new MutableLiveData<MovieListResults>();
    private final MutableLiveData<MovieList> listOfListDetails = new MutableLiveData<MovieList>();
    private final MutableLiveData<MovieResults> listOfMovies = new MutableLiveData<MovieResults>();

    public static final String LANGUAGE = Locale.getDefault().toLanguageTag();


    public ListRepository(Context context) {
        this.mService = RetrofitClient.getInstance().getmRepository();
        this.mContext = context;

        MovieRoomDatabase db = MovieRoomDatabase.getDatabase(context);
        mMovieDAO = db.movieDAO();;
    }

    public MutableLiveData<MovieListResults> getAllLists(int page, User userinfo) {

        Log.d("user", userinfo.getUsername());
        //MutableLiveData<User> user = mUserRepository.getUserInfo();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        Call<MovieListResults> call = mService.getAllLists(userinfo.getId(), mContext.getResources().getString(R.string.api_key), LANGUAGE, session_id, page);
        apiCall(call);

        return listOfLists;
    }

    public MutableLiveData<MovieList> getListDetails(String list_id) {


        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        Call<MovieList> call = mService.getListDetails(list_id, mContext.getResources().getString(R.string.api_key), LANGUAGE);
        call.enqueue(new Callback<MovieList>() {
            @Override
            public void onResponse(Call<MovieList> call, Response<MovieList> response) {
                if(response.code() == 200) {
                    listOfListDetails.setValue(response.body());
                } else {
                    Log.e(UserRepository.class.getSimpleName(), "Something went wrong when retrieving the reviews: \n"
                            + "Response code: " + response.code() + "\n"
                            + "Response body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<MovieList> call, Throwable t) {
                Log.e(UserRepository.class.getSimpleName(), "Something went wrong when starting the request to retrieve the reviews");
            }
        });


        return listOfListDetails;
    }

    public void addList(String name, String description) {

        Log.d("name", name);
        Log.d("desc", description);
        //MutableLiveData<User> user = mUserRepository.getUserInfo();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        MovieListData body = new MovieListData(name, description);

        Call<MovieListResults> call = mService.createList(mContext.getResources().getString(R.string.api_key), session_id, LANGUAGE, body);
        apiCall(call);
        //return listOfLists;
    }

    public void addMovie(String list_id, int movieId) {
        //HashMap<String, Integer> movieData = new HashMap<>();
       // Log.d("name", name);
       // Log.d("desc", description);
        //movieData.put("media_id", movieId);
        //MutableLiveData<User> user = mUserRepository.getUserInfo();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        Log.d("apikey", mContext.getResources().getString(R.string.api_key));
        Log.d("session", session_id);


       // MovieListData body = new MovieListData(name, description);
        MovieData body = new MovieData(movieId);

        Call<MovieResults> call = mService.addMovie(list_id, mContext.getResources().getString(R.string.api_key), session_id, body);

        call.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                if(response.code() == 201) {
                    listOfMovies.setValue(response.body());
                } else {
                    Log.e(UserRepository.class.getSimpleName(), "Something went wrong when retrieving the reviews: \n"
                            + "Response code: " + response.code() + "\n"
                            + "Response body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                Log.e(UserRepository.class.getSimpleName(), "Something went wrong when starting the request to retrieve the reviews");
            }
        });
        //return listOfLists;
    }

    public void deleteMovie(String list_id, int movieId) {
        //HashMap<String, Integer> movieData = new HashMap<>();
        // Log.d("name", name);
        // Log.d("desc", description);
        //movieData.put("media_id", movieId);
        //MutableLiveData<User> user = mUserRepository.getUserInfo();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        Log.d("apikey", mContext.getResources().getString(R.string.api_key));
        Log.d("session", session_id);


        // MovieListData body = new MovieListData(name, description);
        MovieData body = new MovieData(movieId);

        Call<MovieResults> call = mService.deleteMovie(list_id, mContext.getResources().getString(R.string.api_key), session_id, body);

        call.enqueue(new Callback<MovieResults>() {
            @Override
            public void onResponse(Call<MovieResults> call, Response<MovieResults> response) {
                if(response.code() == 200) {
                    listOfMovies.setValue(response.body());
                } else {
                    Log.e(UserRepository.class.getSimpleName(), "Something went wrong when retrieving the reviews: \n"
                            + "Response code: " + response.code() + "\n"
                            + "Response body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<MovieResults> call, Throwable t) {
                Log.e(UserRepository.class.getSimpleName(), "Something went wrong when starting the request to retrieve the reviews");
            }
        });
        //return listOfLists;
    }

    public void deleteList(String list_id) {
        //HashMap<String, Integer> movieData = new HashMap<>();
        // Log.d("name", name);
        // Log.d("desc", description);
        //movieData.put("media_id", movieId);
        //MutableLiveData<User> user = mUserRepository.getUserInfo();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String session_id = sharedPreferences.getString(SESSION_ID, null);

        Log.d("apikey", mContext.getResources().getString(R.string.api_key));
        Log.d("session", session_id);


        Call<MovieListResults> call = mService.deleteList(list_id, mContext.getResources().getString(R.string.api_key), session_id);

        call.enqueue(new Callback<MovieListResults>() {
            @Override
            public void onResponse(Call<MovieListResults> call, Response<MovieListResults> response) {
                if(response.code() == 200) {
                    //listOfMovies.setValue(response.body());
                } else {
                    Log.e(UserRepository.class.getSimpleName(), "Something went wrong when retrieving the reviews: \n"
                            + "Response code: " + response.code() + "\n"
                            + "Response body: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<MovieListResults> call, Throwable t) {
                Log.e(UserRepository.class.getSimpleName(), "Something went wrong when starting the request to retrieve the reviews");
            }
        });
        //return listOfLists;
    }



    private void apiCall(Call<MovieListResults> call){
        call.enqueue(new Callback<MovieListResults>() {
            @Override
            public void onResponse(Call<MovieListResults> call, Response<MovieListResults> response) {
                listOfLists.setValue(response.body());
            }

            @Override
            public void onFailure(Call<MovieListResults> call, Throwable t) {
                listOfLists.postValue(null);
            }
        });
    }


}