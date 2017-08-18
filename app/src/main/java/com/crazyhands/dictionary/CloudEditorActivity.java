package com.crazyhands.dictionary;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.ListView;
import android.widget.Spinner;
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
import com.crazyhands.dictionary.Adapters.CantoneseListAdapter;
import com.crazyhands.dictionary.App.Config;
import com.crazyhands.dictionary.Fragments.BaseActivity;
import com.crazyhands.dictionary.data.Contract.WordEntry;
import com.crazyhands.dictionary.data.MediaPlayeHelperClass;
import com.crazyhands.dictionary.data.QueryUtils;
import com.crazyhands.dictionary.items.Cantonese_List_item;


import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.App.Config.URL_DELEAT_CANTONESE_WHERE_ID;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE_ID;

public class CloudEditorActivity extends AppCompatActivity implements Response.ErrorListener{


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

    /** EditText field to enter the pet's gender */
    private Spinner mTypeSpinner;

    /** Boolean flag that keeps track of whether the word has been edited (true) or not (false) */
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
                            Toast.LENGTH_LONG).show();
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

                Toast.makeText(CloudEditorActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
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
                Intent intent = new Intent(CloudEditorActivity.this, BaseActivity.class);
                startActivity(intent);
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deleateWord();
                Intent iintent = new Intent(CloudEditorActivity.this, BaseActivity.class);
                startActivity(iintent);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (BaseActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(CloudEditorActivity.this, "noo it didn't send", Toast.LENGTH_LONG).show();
            return;
        } else {
            uploadMultipart(englishString, jyutpingString, cantoneseString, soundstring);
            Toast.makeText(CloudEditorActivity.this, "yay it sent", Toast.LENGTH_LONG).show();
        }
    }

    /*
    * This is the method responsible for image upload
    * We need the full image path and the name for the image in this method
    * */
    public void uploadMultipart(final String englishString, final String jyutpingString, final String cantoneseString, final String soundstring) {
        //getting name for the image
        //getting the actual path of the image
        String path;
        if (recorded == true) {
            path = getFilesDir() + "/" + soundstring;//this may be wrong todo check
            Log.v("file plus sound str is:", getFilesDir() + "/" + soundstring);

        } else {
            path = null;
        }
        if (recorded == true) {

            //Uploading code
            try {
                String uploadId = UUID.randomUUID().toString();
                //Creating a multi part request
                new MultipartUploadRequest(this, uploadId, Config.URL_ADD_WORD)
                        .addFileToUpload(path, "userfile") //Adding file
                        .addParameter("id", Integer.toString(wordid))
                        .addParameter("name", soundstring) //Adding text parameter to the request
                        .addParameter("type", String.valueOf(mType))
                        .addParameter("jyutping", jyutpingString)
                        .addParameter("english", englishString)
                        .addParameter("cantonese", cantoneseString)
                        .addParameter("soundAddress", soundstring)
                        .setMaxRetries(2)
                        .setUtf8Charset()
                        .startUpload(); //Starting the upload

            } catch (Exception exc) {
                Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }}
        else {
            final RequestQueue requestque = Volley.newRequestQueue(CloudEditorActivity.this);

            StringRequest request = new StringRequest(Request.Method.POST, Config.URL_ADD_WORD,

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

                            requestque.stop();


                        }
                    },this) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting params to register url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("id", Integer.toString(wordid));
                    params.put("name", soundstring); //Adding text parameter to the request
                    params.put("type", String.valueOf(mType));
                    params.put("english", englishString);
                    params.put("jyutping", jyutpingString);
                    params.put("cantonese", cantoneseString);
                    params.put("soundAddress", soundstring);

                    return params;

                }

            };
            requestque.add(request);

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

    private void addValuesToFields(int wordid) {
        mEnglishEditText.setText("things english");
        mJyutpingEditText.setText("things juytping");
        mCantoneseEditText.setText("things cantonese");
        //mSoundtextview.setText("things sound location");
//todo change the spinner

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
                },this);
        requestque.add(request);


    }

    private void deleateWord() {

        final RequestQueue requestque = Volley.newRequestQueue(CloudEditorActivity.this);

        StringRequest request = new StringRequest(Request.Method.POST, URL_DELEAT_CANTONESE_WHERE_ID + "/?id=" + wordid,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response

                        // If the JSON string is empty or null, then return early.
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }


                        requestque.stop();


                    }
                },this) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                //params.put("profile_picture", new FileBody(new File("/storage/emulated/0/Pictures/VSCOCam/2015-07-31 11.55.14 1.jpg")));
                params.put("id", String.valueOf(wordid));


                return params;

            }

        };
        requestque.add(request);

    }
    @Override
    public void onErrorResponse(VolleyError volleyError) {
        //textview.setText("someshit gone down!");
        volleyError.printStackTrace();
        Log.e(TAG, "Response error" + volleyError.getMessage());
        Toast.makeText(CloudEditorActivity.this,
                volleyError.getMessage(), Toast.LENGTH_LONG).show();
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

        Toast.makeText(CloudEditorActivity.this, message, Toast.LENGTH_SHORT).show();

    }
}
