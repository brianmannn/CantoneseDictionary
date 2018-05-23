package com.crazyhands.dictionary.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import com.crazyhands.dictionary.Adapters.WordCursorAdapter;
import com.crazyhands.dictionary.R;
import com.crazyhands.dictionary.data.Contract;



public class BasicWordsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private WordCursorAdapter mAdapter;


    /**
     * Identifier for the pet data loader
     */
    private static final int WORD_LOADER=1;

    public BasicWordsFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
// Inflate the layout for this fragment
        final View rootView=inflater.inflate(R.layout.sqlite_fragment,container,false);


        // Find the ListView which will be populated with the pet data
        ListView wordListView=(ListView)rootView.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView=rootView.findViewById(R.id.empty_view);
        wordListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no word data yet (until the loader finishes) so pass in null for the Cursor.
        mAdapter=new WordCursorAdapter(getContext(),null);
        wordListView.setAdapter(mAdapter);

        // Kick off the loader
        getLoaderManager().initLoader(WORD_LOADER,null,this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        // Define a projection that specifies the columns from the table we care about.
        String[]projection={
                Contract.WordEntry._ID,
                Contract.WordEntry.COLUMN_DICTIONARY_ENGLISH,
                Contract.WordEntry.COLUMN_DICTIONARY_JYUTPING,
                Contract.WordEntry.COLUMN_DICTIONARY_CANTONESE,
                Contract.WordEntry.COLUMN_DICTIONARY_SOUND_ID,
                Contract.WordEntry.COLUMN_DICTIONARY_TYPE};

        String selection=Contract.WordEntry.COLUMN_DICTIONARY_TYPE+"=?";
        String[]selectionArgs={String.valueOf(Contract.WordEntry.TYPE_BASIC)};


        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getContext(),   // Parent activity context
                Contract.WordEntry.CONTENT_URI,     // Provider content URI to query
                projection,                         // Columns to include in the resulting Cursor
                selection,                          // No selection clause
                selectionArgs,                      // No selection arguments
                null);                              // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor data){
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        // Callback called when the data needs to be deleted
        mAdapter.swapCursor(null);
    }
}