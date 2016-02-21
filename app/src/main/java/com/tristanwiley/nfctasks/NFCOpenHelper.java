package com.tristanwiley.nfctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class NFCOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "nfcompanion.db";
    private static final int DATABASE_VERSION = 1;

    public NFCOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        buildTagTable(db);
        buildNestTaskTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void buildTagTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + NFContract.TagEntry.TABLE_NAME + " (" +
                        NFContract.TagEntry._ID + " INTEGER PRIMARY KEY, " +
                        NFContract.TagEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL);"
        );
    }

    private void buildNestTaskTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + NFContract.NestTaskEntry.TABLE_NAME + " (" +
                        NFContract.NestTaskEntry._ID + " INTEGER PRIMARY KEY, " +
                        NFContract.NestTaskEntry.COLUMN_TAG + " TEXT NOT NULL, " +
                        NFContract.NestTaskEntry.COLUMN_TARGET_VALUE + " INTEGER NOT NULL, " +
                        "FOREIGN KEY (" + NFContract.NestTaskEntry.COLUMN_TARGET_VALUE + ") " +
                        "REFERENCES " + NFContract.TagEntry.TABLE_NAME + " (" + NFContract.TagEntry.COLUMN_NAME + "));"
        );
    }
}
