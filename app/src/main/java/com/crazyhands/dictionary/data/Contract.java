package com.crazyhands.dictionary.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;



public  final class Contract {


    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private Contract() {}


    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */

    public static final String CONTENT_AUTHORITY = "com.crazyhands.dictionary";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_DICTIONARY = "dictionary";

    /**
     * Inner class that defines constant values for the words database table.
     * Each entry in the table represents a single word.
     */

    public static abstract class WordEntry implements BaseColumns {

        /** The content URI to access the word data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DICTIONARY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of words.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DICTIONARY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single word.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DICTIONARY;

        /** Name of database table for words */

        public static final String TABLE_NAME = "dictionary";

        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _id = "_id";

        /**
         * Name of the word in english.
         *
         * Type: TEXT
         */
        public static final String COLUMN_DICTIONARY_ENGLISH = "English";

        /**
         * the word in Jyutping
         *
         * Type: TEXT
         */
        public static final String COLUMN_DICTIONARY_JYUTPING = "Jyutping";
        /**
         * the word in Cantonese
         *
         * Type: TEXT
         */

        public static final String COLUMN_DICTIONARY_CANTONESE = "Cantonese";

        /**
         * the sound of the word in Cantonese
         *
         * Type: TEXT
         */

        public static final String COLUMN_DICTIONARY_SOUND_ID = "Sound_id";

        /**
         * the sound of the word in Cantonese
         *
         * Type: int
         */

        public static final String COLUMN_DICTIONARY_TYPE = "type";



        /**
         * Possible values for the type of the word.
         */
        public static final int TYPE_ALL = 0;
        public static final int TYPE_BASIC = 1;
        public static final int TYPE_PHRASE = 2;
        public static final int TYPE_NUMBER = 3;

        /**
         * Returns whether or not the given gender is {@link #TYPE_ALL}, {@link #TYPE_BASIC},
         * or {@link #TYPE_PHRASE}.
         */
        public static boolean isValidType(int type) {
            if (type == TYPE_ALL || type == TYPE_BASIC || type == TYPE_PHRASE || type == TYPE_NUMBER) {
                return true;
            }
            return false;
        }
    }
}