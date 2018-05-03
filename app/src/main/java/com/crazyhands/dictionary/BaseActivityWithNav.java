package com.crazyhands.dictionary;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crazyhands.dictionary.Fragments.*;
import com.crazyhands.dictionary.Fragments.BasicWordsFragment;
import com.crazyhands.dictionary.data.Contract.WordEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.R.id.refresh;
import static com.crazyhands.dictionary.R.id.search_button;
import static com.crazyhands.dictionary.R.id.search_edit_text;

public class BaseActivityWithNav extends AppCompatActivity implements Response.ErrorListener{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_with_nav);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        //imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        //search function
        searchButton = (Button) navHeader.findViewById(search_button);
        searchField = (EditText) navHeader.findViewById(search_edit_text);

        //set intent to open the CloudEditorActivity on the fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BaseActivityWithNav.this, CloudEditorActivity.class));
                drawer.closeDrawers();
            }
        });

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }


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
            toggleFab();
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
                if(bundle != null){fragment.setArguments(bundle);};
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
        toggleFab();

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
                // movies fragment
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
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

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
        if (navItemIndex == 0 || navItemIndex == 1 || navItemIndex == 2 || navItemIndex == 3 || navItemIndex == 4) {
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
            return true;
        }

        //if refresh button is pressed
        if (id == R.id.refresh) {
            syncSQLiteMySQLDB();
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

    //methods for syncing the databases!

    // Method to Sync MySQL to SQLite DB
    public void syncSQLiteMySQLDB() {


        // Show ProgressBar
        prgDialog.show();

        final RequestQueue requestque = Volley.newRequestQueue(BaseActivityWithNav.this);

        StringRequest request = new StringRequest(Request.Method.POST, "http://s681173862.websitehome.co.uk/ian/Dictionary/getCantonese.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley mainactivity", "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response
                        // Hide ProgressBar
                        prgDialog.hide();
                        // If the JSON string is empty or null, then return early.
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }else{
                            Log.v("response", response);
                            // Hide ProgressBar
                            prgDialog.hide();
                            // Update SQLite DB with response sent by getCantonese.php
                            updateSQLite(response);
                        }
                        // Try to parse the JSON response string. If there's a problem with the way the JSON
                        // is formatted, a JSONException exception object will be thrown.
                        // Catch the exception so the app doesn't crash, and print the error message to the logs.

                        requestque.stop();


                    }
                },this);

        requestque.add(request);



    }





    public void updateSQLite(String response){
        ArrayList<HashMap<String, String>> usersynclist;
        usersynclist = new ArrayList<HashMap<String, String>>();
        // Create GSON object
        Gson gson = new GsonBuilder().create();
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(response);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or events).
            JSONArray arr = baseJsonResponse.getJSONArray("result");

            // Extract JSON array from the response
            //JSONArray arr = new JSONArray(eventsarray);

            System.out.println(arr.length());
            // If no of array elements is not zero
            if(arr.length() != 0){
                // Loop through each array element, get JSON object which has english, jyutping, cantonese and soundid
                for (int i = 0; i < arr.length(); i++) {
                    // Get JSON object
                    JSONObject obj = (JSONObject) arr.get(i);
                    System.out.println(obj.get("English"));
                    System.out.println(obj.get("jyutping"));
                    System.out.println(obj.get("cantonese"));
                    System.out.println(obj.get("soundAddress"));
                    System.out.println(obj.get("type"));

                    // Create a ContentValues object where column names are the keys,
                    // and word attributes from the json are the values.
                    ContentValues values = new ContentValues();

                    // Add english extracted from Object
                    values.put(WordEntry._id, obj.get("id").toString());

                    // Add english extracted from Object
                    values.put(WordEntry.COLUMN_DICTIONARY_ENGLISH, obj.get("English").toString());

                    // Add jyutping extracted from Object
                    values.put(WordEntry.COLUMN_DICTIONARY_JYUTPING, obj.get("jyutping").toString());

                    // Add cantonese extracted from Object
                    values.put(WordEntry.COLUMN_DICTIONARY_CANTONESE, obj.get("cantonese").toString());

                    // Add cantonese extracted from Object
                    values.put(WordEntry.COLUMN_DICTIONARY_SOUND_ID, obj.get("soundAddress").toString());

                    // Add type extracted from Object
                    values.put(WordEntry.COLUMN_DICTIONARY_TYPE, obj.get("type").toString());

                    // Insert word into SQLite DB
                    Uri newUri = getContentResolver().insert(WordEntry.CONTENT_URI, values);
                    Log.i("newuri", String.valueOf(newUri));


                    //hashmap for sync status of all the words just added
                    HashMap<String, String> map = new HashMap<String, String>();
                    // Add status for each User in Hashmap
                    map.put("id", obj.get("id").toString());
                    map.put("status", "1");
                    usersynclist.add(map);
                }//todo delete words in sqlite that are not in mysql
                // Inform Remote MySQL DB about the completion of Sync activity by passing Sync status of Users
                //updateMySQLSyncSts(gson.toJson(usersynclist)); //todo enable this?
                // Reload the Main Activity
                reloadActivity();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Method to inform remote MySQL DB about completion of Sync activity
    public void updateMySQLSyncSts(final String json) {
        System.out.println(json);
        // Make Http call to updatesyncsts.php with JSON parameter which has Sync statuses of Users

//volley version


        final RequestQueue requestque = Volley.newRequestQueue(BaseActivityWithNav.this);

        StringRequest request = new StringRequest(Request.Method.POST, "http://s681173862.websitehome.co.uk/ian/dictionary/updatesyncsts.php",
    //todo change url
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("volley mainactivity", "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response
                        Toast.makeText(getApplicationContext(),	"MySQL DB has been informed about Sync activity", Toast.LENGTH_LONG).show();

                        // If the JSON string is empty or null, then return early.
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }else{Log.v("syncsts", response);
                        }
                        // Try to parse the JSON response string. If there's a problem with the way the JSON
                        // is formatted, a JSONException exception object will be thrown.
                        // Catch the exception so the app doesn't crash, and print the error message to the logs.

                        requestque.stop();


                    }
                },this) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("syncsts", json);

                return params;

            }

        };
        requestque.add(request);

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
