package com.crazyhands.dictionary.data;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.crazyhands.dictionary.items.Cantonese_List_item;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class QueryUtils {

    public static List<Cantonese_List_item> extractDataFromJson(String response) {


        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding events to
        List<Cantonese_List_item> words = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(response);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or events).
            JSONArray eventsarray = baseJsonResponse.getJSONArray("result");

            // For each word in the eventsarray, create an {@link Event} object
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

                // Create a new {@link List_item} object with the name, dste, time,
                // from the JSON response.
                Cantonese_List_item word = new Cantonese_List_item(id, english, jyutping, cantonese, soundAddress);

                // Add the new {@link Earthquake} to the list of events.
                words.add(word);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the JSON results", e);
        }

    // Return the list of events
        return words;
}


}

