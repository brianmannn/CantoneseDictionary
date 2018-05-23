package com.crazyhands.dictionary;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crazyhands.dictionary.Api.Model.POJ;
import com.crazyhands.dictionary.Api.Model.Word;
import com.crazyhands.dictionary.Api.Service.DictionaryClient;
import com.crazyhands.dictionary.App.Config;
import com.crazyhands.dictionary.data.Contract.WordEntry;


import net.gotev.uploadservice.MultipartUploadRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.RECORD_AUDIO;

public class CloudEditorActivity extends AppCompatActivity {


    private static Object httpClient;
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

    private int mType = 0;


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
            // This is a new word, so change the app bar to say "Add a Word"
            setTitle(getString(R.string.editor_activity_title_new_word));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a word that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing word, so change app bar to say "Edit Word"
            setTitle(getString(R.string.editor_activity_title_edit_word));
            recorded = false;

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


        // onclick listeners for the sound recorder
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkPermission()) {
                    recorded = true;

                    AudioSavePathInDevice = getOutputMediaFile(MEDIA_TYPE_SOUND);
                    assert AudioSavePathInDevice != null;
                    mSoundtextview.setText(AudioSavePathInDevice.getName());
                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException | IOException e) {
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
        ((LinearLayout) findViewById(R.id.RecorderLinearLayout)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.cloud_soundRecorderTextView)).setVisibility(View.GONE);
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

        //if word id=-1 then it is a new word
        if (wordid == -1) {
            Word word = new Word(englishString, cantoneseString, jyutpingString, soundstring, mType);
            //sendNetworkRequest(word);
            uploadFile(soundstring);

        } else {//else it's a word update
            Word word = new Word(wordid, englishString, cantoneseString, jyutpingString, soundstring, mType);
            UpdateWordNetworkRequest(word);
        }
    }


    private Retrofit buildRetrofit() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        Builder httpClient = new Builder();
        httpClient.addInterceptor(logging);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder().addHeader("Accept", "application/json").addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImE1NDE3NDE3MDdlNWFlZGE3MWUxMGU2NmYxNzk2MmY0MzIzYmZhNTEyMTI3Nzg1YmE0ZmM1Nzk2MWRmZGYwOWFmMmUwOWZmNGE1ODhkMzM4In0.eyJhdWQiOiIyIiwianRpIjoiYTU0MTc0MTcwN2U1YWVkYTcxZTEwZTY2ZjE3OTYyZjQzMjNiZmE1MTIxMjc3ODViYTRmYzU3OTYxZGZkZjA5YWYyZTA5ZmY0YTU4OGQzMzgiLCJpYXQiOjE1MjY4MzcxMDEsIm5iZiI6MTUyNjgzNzEwMSwiZXhwIjoxNTU4MzczMTAxLCJzdWIiOiI2Iiwic2NvcGVzIjpbIioiXX0.Q0MNJn9W6wB67Ty2CIevG7bXzZCzNO0XxGtl9JqaYd9luC39eCFD8pbzTkT_YgXoL5CjiV0LjV8NbMBOYMZ26LsWNzeku05nIv92zFkbHJBiv2OTWLVBIZ4e39jFp6gLat--SkdJaOBAPheiSFJEwSIaTA1VbsveM4LtsaUAs0UKsuOJEjnkx3yUiahg8W32JC19MT5P1osD7ckes8rnA_XDjgvKbBPb1FlhAR3yN3KNNQjiQV_pqjJrwyGW-RKvxG3_YvUJAyzPW9f7Y9sTDKxeQDIPZ8b8quWlWaSVO93wtd6evmhq_YMWsecojyqh1kxb1Uosq-oblyJL3lpgqE45RdbKlWZDW6ObvHcdC_tFMx2CTgnhf99rrKPcQIQ8QO9wG4j8O_uQh17OjPnNz7FVh-2HHPCTLp5m-tsHjKu6H2ewBSK6PNrHp7cxjF8VI28OkcJz-kzSc3zTA5L3SPElcSxC036xlVT6SsW-oEBZus2KLwBeZB1JzzpgyXPshGy3ZQZL0tXmr7t-boU5dvw4EIsP11V-WjyBoEbbMajzGSbJ8BaIu663XktFm_tGBk9objmV0AD0Yzigrleq3Cavph9_5FT4GvSXResMk3pI1m7Cbsq6feCC6EHXMwcLu9ZD0nXt0TJfk1vEPTfgbpoO8ED8uKWAsZUC9x5v6uY").build();
                return chain.proceed(request);
            }
        });
        return new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl("http://www.brianstein.co.uk/api/").client(httpClient.build()).build();

    }


    //retro fit add a new word to database
    private void sendNetworkRequest(Word word) {
        //create retrofit instance
        Retrofit retrofit = buildRetrofit();

        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        client.CreateWord(word);
        Call<Word> call = client.CreateWord(word);

        call.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull retrofit2.Response<Word> response) {
                Toast.makeText(CloudEditorActivity.this, "word added successfully", Toast.LENGTH_SHORT).show();
                Log.d("CloudEditorActivity", "Events Response: " + response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                //Toast.makeText(MainActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                Log.d("retrofit mainactivity", "something went wrong: " + t.getMessage());
            }
        });

    }


    //volly fill in fields todo change this to sqli call
    private void addValuesToFields(int mwordId) {
        mEnglishEditText.setText(R.string.setEnglish);
        mJyutpingEditText.setText(R.string.setJuytping);
        mCantoneseEditText.setText(R.string.SetCantonese);
        //create retrofit instance
        Retrofit retrofit = buildRetrofit();
        Log.d("content", "adding values");


        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        Call<POJ> call = client.GetWord(mwordId);

        call.enqueue(new Callback<POJ>() {
            @Override
            public void onResponse(@NonNull Call<POJ> call, @NonNull retrofit2.Response<POJ> response) {


                if (response.body() != null) {

                    // Extract the value for the key called "id"
                    int id = response.body().getWords().getId();

//{"data":{"id":1,"english":"Heart","jyutping":"sam","cantonese":"\u5fc3","soundAddress":"sam.3gp","type":1,"syncsts":0,"remember_token":null,"created_at":null,"updated_at":null},"version ":"1.0.0","author_url":"brianstein.co.uk"}
                    // Extract the value for the key called "English"
                    String english = response.body().getWords().getEnglish();
                    Log.d("EditorResponseBody", "response.body().getEnglish() is : " + response.body());

                    // Extract the value for the key called "jyutping",
                    String jyutping = response.body().getWords().getJyutping();

                    // Extract the value for the key called "cantonese"
                    String cantonese = response.body().getWords().getCantonese();

                    // Extract the value for the key called "sound address"
                    String soundAddress = response.body().getWords().getSoundAddress();

                    mEnglishEditText.setText(english);
                    mJyutpingEditText.setText(jyutping);
                    mCantoneseEditText.setText(cantonese);
                    mSoundtextview.setText(soundAddress);

                } else {
                    Toast.makeText(CloudEditorActivity.this, "That word doesn't seem to exist", Toast.LENGTH_SHORT).show();
                    Log.d("editorUpdate", "body was empty");

                }
            }


            @Override
            public void onFailure(@NonNull Call<POJ> call, @NonNull Throwable t) {
                Toast.makeText(CloudEditorActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void UpdateWordNetworkRequest(Word word) {
        //create retrofit instance
        Retrofit retrofit = buildRetrofit();

        //get client and call object for the request
        DictionaryClient client = retrofit.create(DictionaryClient.class);
        client.CreateWord(word);
        Call<POJ> call = client.UpdateWord(word);

        call.enqueue(new Callback<POJ>() {
            @Override
            public void onResponse(@NonNull Call<POJ> call, @NonNull retrofit2.Response<POJ> response) {
                //Toast.makeText(CloudEditorActivity.this, "word updated successfully", Toast.LENGTH_SHORT).show();
                Log.d("EditorActivityUpdate", "Events Response: " + response.toString());
            }

            @Override
            public void onFailure(@NonNull Call<POJ> call, @NonNull Throwable t) {
                Toast.makeText(CloudEditorActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //delete word function
    //returns void

    private void deleteWord() {
        //create retrofit instance
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Builder httpClient = new Builder();

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
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
            public void onResponse(@NonNull Call<Word> call, @NonNull retrofit2.Response<Word> response) {
                //if (response.getId();)
                Toast.makeText(CloudEditorActivity.this, "word deleted", Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                Toast.makeText(CloudEditorActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                Log.d("deleteWord", "fail: " + t.getMessage());

            }
        });


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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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


    /*
     * This is the method responsible for image upload
     * We need the full image path and the name for the image in this method
     * */
    public void uploadMultipart(String soundstring) {
        //getting name for the image
        //getting the actual path of the image
        String path;
        if (recorded == true) {
            path = getFilesDir() + "/" + soundstring;
            Log.v("file plus sound str is:", getFilesDir() + "/" + soundstring);

        } else {
            path = null;
        }
        if (recorded == true) {

            //Uploading code
            try {
                String uploadId = UUID.randomUUID().toString();
                //Creating a multi part request
                MultipartUploadRequest request = new MultipartUploadRequest(this, uploadId, Config.URL_ADD_WORD)
                        .addFileToUpload(path, "userfile") //Adding file
                        .addParameter("id", Integer.toString(wordid))
                        .addParameter("name", soundstring) //Adding text parameter to the request
                        .addParameter("type", String.valueOf(mType))
                        //    .addParameter("jyutping", jyutpingString)
                        //   .addParameter("english", englishString)
                        //   .addParameter("cantonese", cantoneseString)
                        .addParameter("soundAddress", soundstring)
                        .setMaxRetries(2)
                        .setUtf8Charset();
                request.startUpload(); //Starting the upload
                Log.d("multipartUpload", "uploading");

            } catch (Exception exc) {
                Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //todo add call for editing without uploading file
        }
    }


    private void uploadFile(String soundstring) {

        //getting name for the image
        //getting the actual path of the image
        String path;

        if (recorded == true) {
            path = getFilesDir() + "/" + soundstring;
            Log.v("file plus sound str is:", getFilesDir() + "/" + soundstring);

        } else {
            path = null;
        }
        File file = null;
        if (path != null) {
            file = new File(path);
        }
        Uri fileUri = Uri.fromFile(file);
        Log.d("file uri: ", fileUri.toString());
        Log.d("file : ", file.toString());
        if (recorded == true) {

            Uploadfile(fileUri);
        } else {
            //todo add call for editing without uploading file
        }
    }

    private void Uploadfile(Uri fileUri) {


        File file = new File(fileUri.getPath());


        // create upload service client
        Retrofit retrofit = buildRetrofit();
        DictionaryClient client = retrofit.create(DictionaryClient.class);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", file.getName(), requestFile);



        // finally, execute the request
        Call<ResponseBody> call = client.upload(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d("Upload", "success");

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Upload:", t.getMessage());
            }
        });
    }
}
