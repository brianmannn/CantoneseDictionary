package com.crazyhands.dictionary.data;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhands.dictionary.CloudEditorActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by crazyhands on 23/05/2017.
 */


public class MediaPlayeHelperClass  extends AppCompatActivity {

    File AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;

    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;

    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_SOUND_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_SOUND = 2;

    Context mcontext;
    Activity mactivity;
    EditText mJyutpingEditText;

    public void MediaPlayeHelperClass(Context context, Activity activity, EditText JyutpingEditText) {
        mcontext = context;
        mactivity = activity;
        mJyutpingEditText = JyutpingEditText;
    }


    public void startRecording(TextView mSoundtextview, Button buttonStart, Button buttonStop) {


        if (checkPermission()) {

            AudioSavePathInDevice = getOutputMediaFile(MEDIA_TYPE_SOUND);
            mSoundtextview.setText(AudioSavePathInDevice.getName());
            MediaRecorderReady();

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);

        Toast.makeText(mactivity, "Recording started",
                Toast.LENGTH_LONG).show();
    } else {
        requestPermission();
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
        ActivityCompat.requestPermissions(mactivity, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(mcontext, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mcontext, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(mcontext,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(mcontext,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_SOUND) {
            String jyutpingString = mJyutpingEditText.getText().toString().trim();
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + jyutpingString + timeStamp + ".3gp");
        } else {
            return null;
        }

        return mediaFile;
    }


}
