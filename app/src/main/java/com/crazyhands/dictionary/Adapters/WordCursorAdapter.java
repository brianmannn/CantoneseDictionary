package com.crazyhands.dictionary.Adapters;


import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhands.dictionary.CloudEditorActivity;
import com.crazyhands.dictionary.R;
import com.crazyhands.dictionary.data.Contract;
import com.crazyhands.dictionary.data.Contract.WordEntry;

import java.io.IOException;

import static java.security.AccessController.getContext;

/**
 * {@link WordCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class WordCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link WordCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public WordCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.word_layout, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {


        ImageView playsoundImageview = (ImageView) view.findViewById(R.id.playbutton);
        ImageView editingTheWordImageView = (ImageView) view.findViewById(R.id.edit_word_button);
        // Find individual views that we want to modify in the list item layout
        TextView CantoneseTextView = (TextView) view.findViewById(R.id.textViewCantonese);
        TextView JyutpingTextView = (TextView) view.findViewById(R.id.textViewJyutping);
        TextView EnglishTextView = (TextView) view.findViewById(R.id.textViewEnglish);

        // Find the columns of pet attributes that we're interested in
        int englishColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_ENGLISH);
        int jyutpingColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_JYUTPING);
        final int cantoneseColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_CANTONESE);
        int sound_idColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_SOUND_ID);
        final int idColumnIndex = cursor.getColumnIndex(WordEntry._ID);


        // Read the pet attributes from the Cursor for the current pet
        String wordEnglish = cursor.getString(englishColumnIndex);
        String wordJyutping = cursor.getString(jyutpingColumnIndex);
        String wordCantonese = cursor.getString(cantoneseColumnIndex);
        final String wordSound_id = cursor.getString(sound_idColumnIndex);
        final int wordid = Integer.valueOf(cursor.getString(idColumnIndex));



        // Update the TextViews with the attributes for the current pet
        CantoneseTextView.setText(wordCantonese);
        JyutpingTextView.setText(wordJyutping);
        EnglishTextView.setText(wordEnglish);



        playsoundImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MediaPlayer mMediaPlayer = new MediaPlayer();
                //Log.v("sound address:", "http://s681173862.websitehome.co.uk/ian/Dictionary/pronuniation_cantonese/" + wordSound_id);
                //http://briansserver.96.lt/CantoneseDictionary/pronuniation_cantonese/SOUND_20170507_222129.3gp
                try {
                    mMediaPlayer.setDataSource("http://s681173862.websitehome.co.uk/ian/Dictionary/pronuniation_cantonese/" + wordSound_id);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                //http://www.briansserver.96.lt/pronuniation_cantonese/SOUND_20170507_222129.3gp
                ///storage/emulated/0/Pictures/Hello Camera/SOUND_20170507_220116.3gp
                //final MediaPlayer mMediaPlayer = MediaPlayer.create(context, SoundinCantonese);
                mMediaPlayer.start();
                Toast.makeText(context, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });


        editingTheWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create new intent to go to {@link }
                Intent intent = new Intent(context, CloudEditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.words/words/2"
                // if the word with ID 2 was clicked on.
                Uri currentWordUri = ContentUris.withAppendedId(Contract.WordEntry.CONTENT_URI, wordid);
                // Set the URI on the data field of the intent
                intent.setData(currentWordUri);
                intent.putExtra("wordid", wordid);

                // Launch the {@link } to display the data for the current word.
                context.startActivity(intent);


            }
        });

    }
}
