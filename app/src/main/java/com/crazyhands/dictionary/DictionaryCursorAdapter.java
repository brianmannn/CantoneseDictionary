package com.crazyhands.dictionary;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhands.dictionary.data.Contract.WordEntry;

import java.io.IOException;


public class DictionaryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link DictionaryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public DictionaryCursorAdapter(Context context, Cursor c) {
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
        // Find individual views that we want to modify in the list item layout

        TextView englishTextView = (TextView) view.findViewById(R.id.textViewEnglish);
        TextView jyutpingTextView = (TextView) view.findViewById(R.id.textViewJyutping);
        TextView cantoneseTextView = (TextView) view.findViewById(R.id.textViewCantonese);
        ImageView playsoundImageview = (ImageView) view.findViewById(R.id.playbutton);
        ImageView editingTheWordImageView = (ImageView) view.findViewById(R.id.edit_word_button);


        // Find the columns of words attributes that we're interested in
        int idColumnindex = cursor.getColumnIndex(WordEntry._id);
        int englishColumnindex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_ENGLISH);
        int JYUTPINGColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_JYUTPING);
        int CantoneseColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_CANTONESE);
        final int SoundColumnIndex = cursor.getColumnIndex(WordEntry.COLUMN_DICTIONARY_SOUND_ID);
        //TODO add images and links to edit?
        // Read the pet attributes from the Cursor for the current word
        String wordInEnglish = cursor.getString(englishColumnindex);
        String wordInJyutping = cursor.getString(JYUTPINGColumnIndex);
        String wordInCantonese = cursor.getString(CantoneseColumnIndex);
        final int id_ = cursor.getInt(idColumnindex);
        final String SoundinCantonese = cursor.getString(SoundColumnIndex);

        // Update the TextViews with the attributes for the cu rrent word
        englishTextView.setText(wordInEnglish);
        jyutpingTextView.setText(wordInJyutping);
        cantoneseTextView.setText(wordInCantonese);

        playsoundImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MediaPlayer mMediaPlayer = new MediaPlayer();
                try {
                    mMediaPlayer.setDataSource("/storage/emulated/0/Pictures/Hello Camera/" + SoundinCantonese);
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
                //Intent intent = new Intent(context, );

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.words/words/2"
                // if the word with ID 2 was clicked on.
                Uri currentWordUri = ContentUris.withAppendedId(WordEntry.CONTENT_URI, id_);

                // Set the URI on the data field of the intent
                //intent.setData(currentWordUri);

                // Launch the {@link } to display the data for the current word.
                //context.startActivity(intent);


            }
        });
    }
}