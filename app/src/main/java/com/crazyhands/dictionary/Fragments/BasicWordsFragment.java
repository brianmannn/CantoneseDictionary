package com.crazyhands.dictionary.Fragments;

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
import java.util.List;

import static com.android.volley.VolleyLog.TAG;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE;
import static com.crazyhands.dictionary.App.Config.URL_GET_CANTONESE_WHERE;

/**
 * Created by crazyhands on 26/05/2017.
 */

public class BasicWordsFragment extends Fragment {

    private CantoneseListAdapter mAdapter;

    public BasicWordsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_all_list, container, false);




        final RequestQueue requestque = Volley.newRequestQueue(getActivity());

        StringRequest request = new StringRequest(Request.Method.GET, URL_GET_CANTONESE_WHERE+"/?type=basic",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Events Response: " + response.toString());

                        // Extract relevant fields from the JSON response and create a list of  List_items
                        final List<Cantonese_List_item> eventss = QueryUtils.extractDataFromJson(response);

                        // Find the ListView which will be populated with the word data
                        ListView wordListView = (ListView) rootView.findViewById(R.id.list);

                        // Create a new adapter that takes an empty list of events as input
                        mAdapter = new CantoneseListAdapter(getActivity(), new ArrayList<Cantonese_List_item>());

                        // Set the adapter on the {@link ListView}
                        // so the list can be populated in the user interface
                        wordListView.setAdapter(mAdapter);


                        if (eventss != null && !eventss.isEmpty()) {
                            mAdapter.addAll(eventss);
                            // View loadingIndicator = findViewById(R.id.loading_indicator);
                            // loadingIndicator.setVisibility(View.GONE);
                        }

                        requestque.stop();


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //textview.setText("someshit gone down!");
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
                });
        requestque.add(request);

        return rootView;

    }


}
