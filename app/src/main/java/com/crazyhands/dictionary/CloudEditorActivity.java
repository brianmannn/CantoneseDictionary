package com.crazyhands.dictionary;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crazyhands.dictionary.Api.Model.Word;
import com.crazyhands.dictionary.Api.Service.DictionaryClient;
import com.crazyhands.dictionary.data.Contract.WordEntry;


import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE_ID;

public class CloudEditorActivity extends AppCompatActivity implements Response.ErrorListener {


    /**
     * Content URI for the existing word (null if it's a new word)
     */
    private Uri mCurrentWordUri;

    /**
     * EditText field to enter the words's english
     */
    private EditText mEnglishEditText;

    /**
     * EditText field to enter the word's Jyutping
     */
    private EditText mJyutpingEditText;

    /**
     * EditText field to enter the word's Cantonese
     */
    private EditText mCantoneseEditText;

    /**
     * variables for the sound recorder
     */

    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording;
    File AudioSavePathInDevice = null;
    TextView mSoundtextview;
    MediaRecorder mediaRecorder;

    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;

    boolean recorded;
    int wordid = -1;
    // Activity request codes

    public static final int MEDIA_TYPE_SOUND = 2;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mTypeSpinner;

    /**
     * Boolean flag that keeps track of whether the word has been edited (true) or not (false)
     */
    private boolean mWordHasChanged = false;

    private int mType = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mWordHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_editor);


        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        mCurrentWordUri = intent.getData();
        wordid = intent.getIntExtra("wordid", -1);
        Log.v("wordid is", Integer.toString(wordid));


        mEnglishEditText = (EditText) findViewById(R.id.cloud_edit_English);
        mJyutpingEditText = (EditText) findViewById(R.id.cloud_edit_Jyutping);
        mCantoneseEditText = (EditText) findViewById(R.id.cloud_edit_Cantonese);
        mSoundtextview = (TextView) findViewById(R.id.cloud_soundRecorderTextView);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_type);

        // If the intent DOES NOT contain a word content URI, then we know that we are
        // creating a new word.
        if (wordid == -1) {
            // This is a new pet, so change the app bar to say "Add a Word"
            setTitle(getString(R.string.editor_activity_title_new_word));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a word that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing word, so change app bar to say "Edit Word"
            setTitle(getString(R.string.editor_activity_title_edit_word));
            recorded = false;

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            //getLoaderManager().initLoader(EXISTING_WORD_LOADER, null, CloudEditorActivity.this);
            //Todo do I need a loader?
            addValuesToFields(wordid);
        }


        // buttons for the sound recorder

        buttonStart = (Button) findViewById(R.id.cloud_record_button);
        buttonStop = (Button) findViewById(R.id.cloud_stop_button);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.cloud_play_button);
        buttonStopPlayingRecording = (Button) findViewById(R.id.cloud_Rerecord_button);
        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

//spiner on click listener
        mTypeSpinner.setOnTouchListener(mTouchListener);

        // onclick listeners for the sound recorder


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermission()) {
                    recorded = true;

                    AudioSavePathInDevice = getOutputMediaFile(MEDIA_TYPE_SOUND);
                    mSoundtextview.setText(AudioSavePathInDevice.getName());
                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(CloudEditorActivity.this, "Recording started",
                            Toast.LENGTH_SHORT).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);


            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Log.v("the audio path is: ", AudioSavePathInDevice.getPath());

                Toast.makeText(CloudEditorActivity.this, "Recording Playing",
                        Toast.LENGTH_SHORT).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_type_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mTypeSpinner.setAdapter(typeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.type_all))) {
                        mType = WordEntry.TYPE_ALL;
                    } else if (selection.equals(getString(R.string.type_basic))) {
                        mType = WordEntry.TYPE_BASIC;
                    } else if (selection.equals(getString(R.string.type_phrase))) {
                        mType = WordEntry.TYPE_PHRASE;
                    } else {
                        mType = WordEntry.TYPE_NUMBER;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mType = WordEntry.TYPE_ALL;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveWord();
                // Exit activity
                //finish();
                Intent intent = new Intent(CloudEditorActivity.this, BaseActivityWithNav.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deleteWord();
                Intent iintent = new Intent(CloudEditorActivity.this, BaseActivityWithNav.class);
                startActivity(iintent);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (BaseActivityWithNav)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //save new or updated word to a word object

    private void saveWord() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String englishString = mEnglishEditText.getText().toString().trim();
        String jyutpingString = mJyutpingEditText.getText().toString().trim();
        String cantoneseString = mCantoneseEditText.getText().toString().trim();
        String soundstring = mSoundtextview.getText().toString().trim();
        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentWordUri == null &&
                TextUtils.isEmpty(englishString) && TextUtils.isEmpty(jyutpingString) &&
                TextUtils.isEmpty(cantoneseString) &&
                TextUtils.isEmpty(soundstring)) {
            // Since no fields were modified, we can return early without creating a new word.
            return;
        } else {//if word id=-1 then it is a new word
            if (wordid == -1){
                Word word = new Word(englishString, cantoneseString, jyutpingString, soundstring, mType);
            sendNetworkRequest(word);
            }else{//else it's a word update
                Word word = new Word(wordid, englishString, cantoneseString, jyutpingString, soundstring, mType );
                UpdateWordNetworkRequest(word);
            }
        }
    }

    private void UpdateWordNetworkRequest(Word word) {
        //create retrofit instance
        Retrofit retrofit = buildRetrofit();

        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        client.CreateWord(word);
        Call<Word> call = client.UpdateWord(word);

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, retrofit2.Response<Word> response) {
                Toast.makeText(CloudEditorActivity.this, "word updated successfully", Toast.LENGTH_SHORT).show();
                Log.d("CloudEditorActivity", "Events Response: " + response.toString());
            }
            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                Toast.makeText(CloudEditorActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //retro fit add word to database

    private void sendNetworkRequest(Word word) {
        //create retrofit instance
        Retrofit retrofit = buildRetrofit();

        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        client.CreateWord(word);
        Call<Word> call = client.CreateWord(word);

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, retrofit2.Response<Word> response) {
                Toast.makeText(CloudEditorActivity.this, "word added successfully", Toast.LENGTH_SHORT).show();
                Log.d("CloudEditorActivity", "Events Response: " + response.toString());
            }

            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                //Toast.makeText(MainActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                Log.d("retrofit mainactivity", "something went wrong: " + t.getMessage());

            }
        });

    }

    private Retrofit buildRetrofit() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder().addHeader("Accept", "application/json").addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImE1NDE3NDE3MDdlNWFlZGE3MWUxMGU2NmYxNzk2MmY0MzIzYmZhNTEyMTI3Nzg1YmE0ZmM1Nzk2MWRmZGYwOWFmMmUwOWZmNGE1ODhkMzM4In0.eyJhdWQiOiIyIiwianRpIjoiYTU0MTc0MTcwN2U1YWVkYTcxZTEwZTY2ZjE3OTYyZjQzMjNiZmE1MTIxMjc3ODViYTRmYzU3OTYxZGZkZjA5YWYyZTA5ZmY0YTU4OGQzMzgiLCJpYXQiOjE1MjY4MzcxMDEsIm5iZiI6MTUyNjgzNzEwMSwiZXhwIjoxNTU4MzczMTAxLCJzdWIiOiI2Iiwic2NvcGVzIjpbIioiXX0.Q0MNJn9W6wB67Ty2CIevG7bXzZCzNO0XxGtl9JqaYd9luC39eCFD8pbzTkT_YgXoL5CjiV0LjV8NbMBOYMZ26LsWNzeku05nIv92zFkbHJBiv2OTWLVBIZ4e39jFp6gLat--SkdJaOBAPheiSFJEwSIaTA1VbsveM4LtsaUAs0UKsuOJEjnkx3yUiahg8W32JC19MT5P1osD7ckes8rnA_XDjgvKbBPb1FlhAR3yN3KNNQjiQV_pqjJrwyGW-RKvxG3_YvUJAyzPW9f7Y9sTDKxeQDIPZ8b8quWlWaSVO93wtd6evmhq_YMWsecojyqh1kxb1Uosq-oblyJL3lpgqE45RdbKlWZDW6ObvHcdC_tFMx2CTgnhf99rrKPcQIQ8QO9wG4j8O_uQh17OjPnNz7FVh-2HHPCTLp5m-tsHjKu6H2ewBSK6PNrHp7cxjF8VI28OkcJz-kzSc3zTA5L3SPElcSxC036xlVT6SsW-oEBZus2KLwBeZB1JzzpgyXPshGy3ZQZL0tXmr7t-boU5dvw4EIsP11V-WjyBoEbbMajzGSbJ8BaIu663XktFm_tGBk9objmV0AD0Yzigrleq3Cavph9_5FT4GvSXResMk3pI1m7Cbsq6feCC6EHXMwcLu9ZD0nXt0TJfk1vEPTfgbpoO8ED8uKWAsZUC9x5v6uY").build();
                return chain.proceed(request);
            }
        });
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("http://www.brianstein.co.uk/api/").client(httpClient.build()).build();
        return retrofit;

    }

    /*
     * This is the method responsible for image upload
     * We need the full image path and the name for the image in this method
     * */
    public void uploadMultipart(final String englishString, final String jyutpingString, final String cantoneseString, final String soundstring) {
        //getting name for the image
        //getting the actual path of the image
        String url = "http://www.brianstein.co.uk/api/dictionary";
//        Config.URL_ADD_WORD

        String path;
        if (recorded == true) {
            path = getFilesDir() + "/" + soundstring;//this may be wrong todo check
            Log.v("file plus sound str is:", getFilesDir() + "/" + soundstring);

        } else {
            path = null;
        }
        if (recorded == true) {

            //Uploading multipart for sound file
            try {
                String uploadId = UUID.randomUUID().toString();
                //Creating a multi part request
                new MultipartUploadRequest(this, uploadId, url)
                        .addFileToUpload(path, "userfile") //Adding file
                        .addParameter("id", Integer.toString(wordid))
                        .addParameter("name", soundstring) //Adding text parameter to the request
                        .addParameter("type", String.valueOf(mType))
                        .addParameter("jyutping", jyutpingString)
                        .addParameter("english", englishString)
                        .addParameter("cantonese", cantoneseString)
                        .addParameter("soundAddress", soundstring)
                        .addParameter("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjE0YzUwZWViMWRjNmEwMjM4M2YzZWRiMDdmNDg4YTJhN2Y2MmFkOWE4MmZiYWNlZjJlYTQyYmUwN2IzZjE4MjQyOGI3Y2RhNTk1ODQzZGFlIn0.eyJhdWQiOiIyIiwianRpIjoiMTRjNTBlZWIxZGM2YTAyMzgzZjNlZGIwN2Y0ODhhMmE3ZjYyYWQ5YTgyZmJhY2VmMmVhNDJiZTA3YjNmMTgyNDI4YjdjZGE1OTU4NDNkYWUiLCJpYXQiOjE1MjY3MjI5NTAsIm5iZiI6MTUyNjcyMjk1MCwiZXhwIjoxNTU4MjU4OTUwLCJzdWIiOiI2Iiwic2NvcGVzIjpbIioiXX0.DIFHMT-7OEDS3xBeCayAGRScnfmvKSwqC0pqHAk9S4267IO9kBS-CmFWpFUthPoUDsvnDvpO1dwiqmSpllt2uBRan0PUh28Er3RWAJrno0Cvyy2IRJLg3VJj0ZDOqNr3hCIrBf49ezIone7QYw86P8XtuVquT4flhgE08qzp6FVpWlyX79SPcC59FoEyccnd6Vgo-JRdWCw3LqEGNP9plnHpILZVrNP90JDxuBiPV1O9bGI0vhyISSUqhbxeEHZ4lK1u7-IPEwFol-2TSYzzOsjwwGOgT1KavO22a0HlT6oTS_aUFZib0_SaIhkoIndmug4tWC6n2vd6DbPlILkkJTbcHA1STN_3kuFfDsCLCegs6TX_B8jxvDcP5YtcV7RfIsdY6i8YhG1LI4rXjaNjySPK59otLG41A2Dh7miHdUXftWaa3F1qTcziv3KQUHDnOtGlSpGUoF5EYbWcjNJQNlINtLJzBK6jw455YJA2dTm_XuVOFJ01cYMCyYlP-HTQEEjL0l8ksNTHU012AgFoNpYuMrWbvLM_XCDkx02zdxnXx76E8xsaGLDEB9e7N2a60pg-jd9JrsUSOEpIZ19Ha9ksUk6zVQxlVGIri4jBbK3SvVnsTBV9UFco2KXsFv27B0ANOx8p-DSDNv-dzR4S3MdF4K-LFpF8NTTEcphDYdQ")
                        .setMaxRetries(2)
                        .setUtf8Charset()
                        .startUpload(); //Starting the upload

            } catch (Exception exc) {
                Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


//methods for the sound recording

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice.getPath());
    }

//request permissions to use the recorder
    private void requestPermission() {
        ActivityCompat.requestPermissions(CloudEditorActivity.this, new
                String[]{RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean RecordPermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (RecordPermission) {
                        Toast.makeText(CloudEditorActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CloudEditorActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED;
    }

    //getting the sound file
    //returns media file

    private File getOutputMediaFile(int type) {

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_SOUND) {
            String jyutpingString = mJyutpingEditText.getText().toString().trim();
            mediaFile = new File(getFilesDir() + File.separator
                    + jyutpingString + timeStamp + ".3gp");
        } else {
            return null;
        }

        return mediaFile;
    }


    //volly fill in fields todo change this to sqli call
    private void addValuesToFields(int wordid) {
        mEnglishEditText.setText(R.string.setEnglish);
        mJyutpingEditText.setText(R.string.setJuytping);
        mCantoneseEditText.setText(R.string.SetCantonese);


        final RequestQueue requestque = Volley.newRequestQueue(CloudEditorActivity.this);

        StringRequest request = new StringRequest(Request.Method.GET, URL_GET_CANTONESE_WHERE_ID + "/?Wordid=" + wordid,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response

                        // If the JSON string is empty or null, then return early.
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }


                        // Try to parse the JSON response string. If there's a problem with the way the JSON
                        // is formatted, a JSONException exception object will be thrown.
                        // Catch the exception so the app doesn't crash, and print the error message to the logs.
                        try {

                            // Create a JSONObject from the JSON response string
                            JSONObject baseJsonResponse = new JSONObject(response);

                            // Extract the JSONArray associated with the key called "result",
                            // which represents a list of features (or events).
                            JSONArray eventsarray = baseJsonResponse.getJSONArray("result");

                            // For each earthquake in the eventsarray, create an {@link Event} object
                            for (int i = 0; i < eventsarray.length(); i++) {

                                // Get a single event at position i within the list of events
                                JSONObject currentWord = eventsarray.getJSONObject(i);


                                // Extract the value for the key called "id"
                                int id = currentWord.getInt("id");

                                // Extract the value for the key called "English"
                                String english = currentWord.getString("English");

                                // Extract the value for the key called "jyutping",
                                String jyutping = currentWord.getString("jyutping");

                                // Extract the value for the key called "cantonese"
                                String cantonese = currentWord.getString("cantonese");

                                // Extract the value for the key called "sound address"
                                String soundAddress = currentWord.getString("soundAddress");

                                mEnglishEditText.setText(english);
                                mJyutpingEditText.setText(jyutping);
                                mCantoneseEditText.setText(cantonese);
                                mSoundtextview.setText(soundAddress);

                            }

                        } catch (JSONException e) {
                            // If an error is thrown when executing any of the above statements in the "try" block,
                            // catch the exception here, so the app doesn't crash. Print a log message
                            // with the message from the exception.
                            Log.e("QueryUtils", "Problem parsing the JSON results", e);
                        }

                        requestque.stop();


                    }
                }, this);
        requestque.add(request);


    }

    //delete word function
    //returns void

    private void deleteWord() {
        //create retrofit instance
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder().addHeader("Accept", "application/json").addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImE1NDE3NDE3MDdlNWFlZGE3MWUxMGU2NmYxNzk2MmY0MzIzYmZhNTEyMTI3Nzg1YmE0ZmM1Nzk2MWRmZGYwOWFmMmUwOWZmNGE1ODhkMzM4In0.eyJhdWQiOiIyIiwianRpIjoiYTU0MTc0MTcwN2U1YWVkYTcxZTEwZTY2ZjE3OTYyZjQzMjNiZmE1MTIxMjc3ODViYTRmYzU3OTYxZGZkZjA5YWYyZTA5ZmY0YTU4OGQzMzgiLCJpYXQiOjE1MjY4MzcxMDEsIm5iZiI6MTUyNjgzNzEwMSwiZXhwIjoxNTU4MzczMTAxLCJzdWIiOiI2Iiwic2NvcGVzIjpbIioiXX0.Q0MNJn9W6wB67Ty2CIevG7bXzZCzNO0XxGtl9JqaYd9luC39eCFD8pbzTkT_YgXoL5CjiV0LjV8NbMBOYMZ26LsWNzeku05nIv92zFkbHJBiv2OTWLVBIZ4e39jFp6gLat--SkdJaOBAPheiSFJEwSIaTA1VbsveM4LtsaUAs0UKsuOJEjnkx3yUiahg8W32JC19MT5P1osD7ckes8rnA_XDjgvKbBPb1FlhAR3yN3KNNQjiQV_pqjJrwyGW-RKvxG3_YvUJAyzPW9f7Y9sTDKxeQDIPZ8b8quWlWaSVO93wtd6evmhq_YMWsecojyqh1kxb1Uosq-oblyJL3lpgqE45RdbKlWZDW6ObvHcdC_tFMx2CTgnhf99rrKPcQIQ8QO9wG4j8O_uQh17OjPnNz7FVh-2HHPCTLp5m-tsHjKu6H2ewBSK6PNrHp7cxjF8VI28OkcJz-kzSc3zTA5L3SPElcSxC036xlVT6SsW-oEBZus2KLwBeZB1JzzpgyXPshGy3ZQZL0tXmr7t-boU5dvw4EIsP11V-WjyBoEbbMajzGSbJ8BaIu663XktFm_tGBk9objmV0AD0Yzigrleq3Cavph9_5FT4GvSXResMk3pI1m7Cbsq6feCC6EHXMwcLu9ZD0nXt0TJfk1vEPTfgbpoO8ED8uKWAsZUC9x5v6uY").build();
                return chain.proceed(request);
            }
        });
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("http://www.brianstein.co.uk/api/").client(httpClient.build()).build();

        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        client.DeleteWord(wordid);
        Call<Word> call = client.DeleteWord(wordid);

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(Call<Word> call, retrofit2.Response<Word> response) {
                //if (response.getId();)
                Toast.makeText(CloudEditorActivity.this, "word deleted", Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onFailure(Call<Word> call, Throwable t) {
                Toast.makeText(CloudEditorActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                Log.d("deleteWord", "fail: " + t.getMessage());

            }
        });


    }

    //volley error response

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
