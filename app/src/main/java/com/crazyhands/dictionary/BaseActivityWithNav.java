package com.crazyhands.dictionary;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crazyhands.dictionary.Api.Model.Word;
import com.crazyhands.dictionary.Api.Service.DictionaryClient;
import com.crazyhands.dictionary.Fragments.*;
import com.crazyhands.dictionary.Fragments.BasicWordsFragment;
import com.crazyhands.dictionary.data.Contract.WordEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.R.id.refresh;
import static com.crazyhands.dictionary.R.id.search_button;
import static com.crazyhands.dictionary.R.id.search_edit_text;

public class BaseActivityWithNav extends AppCompatActivity implements Response.ErrorListener{

    SharedPreferences prefs = null;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private EditText searchField;
    private Button searchButton;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PHOTOS = "photos";
    private static final String TAG_MOVIES = "movies";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_SEARCH = "search";

    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private Bundle bundle; //todo change to mbundle
    public String msearch;

    //for syncing

    // Progress Dialog Object
    ProgressDialog prgDialog;
//https://stackoverflow.com/questions/7217578/check-if-application-is-on-its-first-run
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_with_nav);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        //fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        //search function
        searchButton = (Button) navHeader.findViewById(search_button);
        searchField = (EditText) navHeader.findViewById(search_edit_text);

/*        //set intent to open the CloudEditorActivity on the fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BaseActivityWithNav.this, CloudEditorActivity.class));
                drawer.closeDrawers();
            }
        });*/

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }

//when the search button is clicked
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msearch = searchField.getText().toString().trim();
                navItemIndex = 5;
                bundle =new Bundle();//todo bundle correctly / add it to the fragment
                bundle.putString("search", msearch);
                CURRENT_TAG = TAG_SEARCH;
                loadHomeFragment();


            }
        });

        // Initialize Progress Dialog properties
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Transferring Data from Remote MySQL DB and Syncing SQLite. Please wait...");
        prgDialog.setCancelable(false);

        //get shared preferences including initial install flag
        prefs = getSharedPreferences("com.crazyhands.dictionary", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            sendNetworkGetWordRequest();
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

//when the app app is restarted
    @Override
    protected void onResume() {
        super.onResume();


    }

    /***
     * Load navigation menu header information
     */
    private void loadNavHeader() {
        //todo
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();
        // set toolbar title
        setToolbarTitle();//// TODO: 04/06/2017 work out why the 5th doesnt exist

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {//todo the is true for search, sorted?
            drawer.closeDrawers();

            // show or hide the fab button
          //  toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                if(bundle != null){fragment.setArguments(bundle);}
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        //toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // basic words
                BasicWordsFragment basicFragment = new BasicWordsFragment();
                return basicFragment;
            case 1:
                // settings fragment
                PhrasesFragment settingsFragment = new PhrasesFragment();
                return settingsFragment;

            case 2:
                // numbers fragment
                NumbersFragment numberFragment = new NumbersFragment();
                return numberFragment;
            case 3:
                // otherwords fragment
                otherWordsFregment otherwords = new otherWordsFregment();
                return otherwords;

            case 4:
            // all words fragment
            sqliteAllWordsFragment allFragment = new sqliteAllWordsFragment();
            return allFragment;

            case 5:
                // search fragment
                SearchFragment search = new SearchFragment();
                return search;
            default:
                return new sqliteAllWordsFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        if (navItemIndex!=5){
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }}

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_photos:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_PHOTOS;
                        break;
                    case R.id.nav_movies:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_MOVIES;
                        break;
                    case R.id.nav_notifications:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_NOTIFICATIONS;
                        break;
                    case R.id.nav_settings:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_SETTINGS;
                        break;
                    case R.id.search_button:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_SEARCH;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0 || navItemIndex == 1 || navItemIndex == 2 || navItemIndex == 3 || navItemIndex == 4 || navItemIndex==5) {
            getMenuInflater().inflate(R.menu.main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //if refresh button is pressed
        if (id == R.id.refresh) {
            sendNetworkGetWordRequest();

            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    // show or hide the fab
    private void toggleFab() {
        if (navItemIndex == 0|| navItemIndex== 1|| navItemIndex== 2|| navItemIndex== 3|| navItemIndex== 4)
            fab.show();
        else
            fab.hide();
    }




    private void sendNetworkGetWordRequest() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient.Builder okhttpclientbuilder = new OkHttpClient.Builder();
        okhttpclientbuilder.addInterceptor(logging);


        OkHttpClient okHttpClient = okhttpclientbuilder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://www.brianstein.co.uk/api/")
                .client(okHttpClient)
                .build();

        DictionaryClient client = retrofit.create(DictionaryClient.class);


        Call<List<Word>> call = client.GetWords();

        call.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(Call<List<Word>> call, retrofit2.Response<List<Word>> response) {
                List<Word> words =response.body();
                updateSQLite(words);
                Log.d("get words call","response");

            }
            @Override
            public void onFailure(Call<List<Word>> call, Throwable t) {
                Log.d("retrofit getrequest", "something went wrong: "+t.getMessage());
            }
        });
    }





    public void updateSQLite(List<Word> response){

        System.out.println(response.size());
        // If no of array elements is not zero
        if(response.size() != 0){
            // clear SQLite databese before updateing it
            getContentResolver().delete(WordEntry.CONTENT_URI,null,null);

            // Loop through each array element, get JSON object which has english, jyutping, cantonese and soundid
            for (int i = 0; i < response.size(); i++) {
                // Get JSON object
                //System.out.println(list.get(i))
                Word obj =  response.get(i);
                System.out.println(obj.getId());
                System.out.println(obj.getEnglish());
                System.out.println(obj.getJyutping());
                System.out.println(obj.getCantonese());
                System.out.println(obj.getSoundAddress());
                System.out.println(obj.getType());

                // Create a ContentValues object where column names are the keys,
                // and word attributes from the json are the values.
                ContentValues values = new ContentValues();

                // Add english extracted from Object
               values.put(WordEntry._id, obj.getId());

                // Add english extracted from Object
                values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, obj.getEnglish());

                // Add jyutping extracted from Object
                values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, obj.getJyutping().toString());

                // Add cantonese extracted from Object
               values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, obj.getCantonese().toString());

                // Add cantonese extracted from Object
                values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, obj.getSoundAddress().toString());

                // Add type extracted from Object
                values.put(WordEntry.COLUMN_DICTIONARY_TYPE, obj.getType().toString());



                // Insert word into SQLite DB
               Uri newUri = getContentResolver().insert(WordEntry.CONTENT_URI, values);
               Log.i("newuri", String.valueOf(newUri));



            }//todo delete words in sqlite that are not in mysql
            // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
            //updateMySQLSyncSts(gson.toJson(usersynclist)); //todo enable this?
            // Reload the Main Activity
            reloadActivity();
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        volleyError.printStackTrace();
        Log.e(TAG, "Response error: " + volleyError.getMessage());

        String message = null;
        if (volleyError instanceof NetworkError) {
            message = getString(R.string.ConnectionErrorMessage);
        } else if (volleyError instanceof ServerError) {
            message = "The server could not be found. Please try again after some time!!";
        } else if (volleyError instanceof AuthFailureError) {
            message = "Cannot connect to Internet...Please check your connection!";
        } else if (volleyError instanceof ParseError) {
            message = "Parsing error! Please try again after some time!!";
        } else if (volleyError instanceof NoConnectionError) {
            message = "Cannot connect to Internet...Please check your connection!";
        } else if (volleyError instanceof TimeoutError) {
            message = "Connection TimeOut! Please check your internet connection.";
        }
        reloadActivity();

        if (message != null){Toast.makeText(BaseActivityWithNav.this, message,
                Toast.LENGTH_SHORT).show();}
    }



    // Reload MainActivity
    public void reloadActivity() {
        Intent objIntent = new Intent(getApplicationContext(), BaseActivityWithNav.class);
        startActivity(objIntent);
    }
}
