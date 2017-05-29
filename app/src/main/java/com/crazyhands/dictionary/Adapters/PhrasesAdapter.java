package com.crazyhands.dictionary.Adapters;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhands.dictionary.CloudEditorActivity;
import com.crazyhands.dictionary.R;
import com.crazyhands.dictionary.data.Contract;
import com.crazyhands.dictionary.items.Cantonese_List_item;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by crazyhands on 26/05/2017.
 */

public class PhrasesAdapter extends ArrayAdapter<Cantonese_List_item> {

    private static final String LOG_TAG = CantoneseListAdapter.class.getSimpleName();

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context    The current context. Used to inflate the layout file.
     * @param allclasses A List of ActivityLayout objects to display in a list
     */
    public PhrasesAdapter(Activity context, ArrayList<Cantonese_List_item> allclasses) {

        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context ,0,allclasses);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The position in the list of data that should be displayed in the
     *                 list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.phrase_layout, parent, false);
        }

        ImageView playsoundImageview = (ImageView) listItemView.findViewById(R.id.phrase_playbutton);
        ImageView editingTheWordImageView = (ImageView) listItemView.findViewById(R.id.phrase_edit_word_button);

        // Get the {@link AndroidFlavor} object located at this position in the list
        final Cantonese_List_item currentListItem = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID textViewEglish
        TextView englishTextView = (TextView) listItemView.findViewById(R.id.phrase_textViewEnglish);
        //Get the english name from the current Cantonese_List_item object and
        // set this text on the english TextView
        englishTextView.setText(currentListItem.getenglish());


        // Find the TextView in the list_item.xml layout with the ID textViewJyutping
        TextView jyutTextView = (TextView) listItemView.findViewById(R.id.phrase_textViewJyutping);

        // Get the jyutping from the current List_item object and
        // set this text on the time TextView
        jyutTextView.setText(currentListItem.getjyut());

        //Find the TextView in the list_item.xml layout with the ID textViewCantonese
        TextView cantonTextView = (TextView) listItemView.findViewById(R.id.phrase_textViewCantonese);

        // Get the cantonese from the current List_item object and
        // set this text on the cantonese TextView
        cantonTextView.setText(currentListItem.getcantonese());



        playsoundImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final MediaPlayer mMediaPlayer = new MediaPlayer();
                Log.v("sound address:", "http://s681173862.websitehome.co.uk/ian/Dictionary/pronuniation_cantonese/" + currentListItem.getsoundaddress());
                //http://briansserver.96.lt/CantoneseDictionary/pronuniation_cantonese/SOUND_20170507_222129.3gp
                try {
                    mMediaPlayer.setDataSource("http://s681173862.websitehome.co.uk/ian/Dictionary/pronuniation_cantonese/" + currentListItem.getsoundaddress());
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                //http://www.briansserver.96.lt/pronuniation_cantonese/SOUND_20170507_222129.3gp
                ///storage/emulated/0/Pictures/Hello Camera/SOUND_20170507_220116.3gp
                //final MediaPlayer mMediaPlayer = MediaPlayer.create(context, SoundinCantonese);
                mMediaPlayer.start();
                Toast.makeText(getContext(), "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });


        editingTheWordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create new intent to go to {@link CloudEditorActivity}
                Intent intent = new Intent(getContext(), CloudEditorActivity.class);

                // Form the content URI that represents the specific pet that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link PetEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.words/words/2"
                // if the word with ID 2 was clicked on.
                Uri currentWordUri = ContentUris.withAppendedId(Contract.WordEntry.CONTENT_URI, currentListItem.getwordid());
                int wordid = currentListItem.getwordid();
                // Set the URI on the data field of the intent
                intent.setData(currentWordUri);
                intent.putExtra("wordid", wordid);

                // Launch the {@link CloudEditorActivity} to display the data for the current word.
                getContext().startActivity(intent);


            }
        });


        // Return the whole list item layout (containing 3 TextViews )
        // so that it can be shown in the ListView
        return listItemView;
    }

}
