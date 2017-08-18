package com.crazyhands.dictionary.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.crazyhands.dictionary.R;
import com.crazyhands.dictionary.data.QueryUtils;
import com.crazyhands.dictionary.items.Cantonese_List_item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.VolleyLog.TAG;


public class SearchFragment extends Fragment {

    String URL_SEARCH_FOR_WORD = "http://s681173862.websitehome.co.uk/ian/Dictionary/searchEnglish.php" + "/?searchWord=";

    private CantoneseListAdapter mAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // get the word to be searched for
        String searchword = getArguments().getString("search");

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_all_list, container, false);

        final String searchString = "love";

        final RequestQueue requestque = Volley.newRequestQueue(getActivity());

        final StringRequest request = new StringRequest(Request.Method.GET, URL_SEARCH_FOR_WORD + searchword,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response and create a list of  List_items
                        final List<Cantonese_List_item> words = QueryUtils.extractDataFromJson(response);

                        // Find the ListView which will be populated with the word data
                        ListView wordListView = (ListView) rootView.findViewById(R.id.list);

                        // Create a new adapter that takes an empty list of events as input
                        mAdapter = new CantoneseListAdapter(getActivity(), new ArrayList<Cantonese_List_item>());

                        // Set the adapter on the {@link ListView}
                        // so the list can be populated in the user interface
                        wordListView.setAdapter(mAdapter);


                        if (words != null && !words.isEmpty()) {
                            mAdapter.addAll(words);
                            // View loadingIndicator = findViewById(R.id.loading_indicator);
                            // loadingIndicator.setVisibility(View.GONE);
                        }

                        requestque.stop();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        Log.e(TAG, "Response error" + volleyError.getMessage());
                        Toast.makeText(getActivity(),
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

                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        requestque.stop();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("searchstring", searchString);

                return params;

            }
        };
        requestque.add(request);

        return rootView;

    }
}
