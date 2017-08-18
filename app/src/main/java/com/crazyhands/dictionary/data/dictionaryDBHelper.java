package com.crazyhands.dictionary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.crazyhands.dictionary.data.Contract.WordEntry;


public class dictionaryDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dictionary.db";

    public dictionaryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {

// Create a String that contains the SQL statement to create the dictionary table table

        String SQL_CREATE_DICTIONARY_TABLE = "CREATE TABLE " + WordEntry.TABLE_NAME + " ("
                + WordEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WordEntry.COLUMN_DICTIONARY_ENGLISH + " TEXT NOT NULL, "
                + WordEntry.COLUMN_DICTIONARY_JYUTPING + " TEXT NOT NULL, "
                + WordEntry.COLUMN_DICTIONARY_CANTONESE + " TEXT NOT NULL, "
                + WordEntry.COLUMN_DICTIONARY_SOUND_ID + " TEXT NOT NULL, "
                + WordEntry.COLUMN_DICTIONARY_TYPE + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_DICTIONARY_TABLE);





    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    }
