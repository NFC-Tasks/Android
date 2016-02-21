package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class NFDataSource {
    private Context mContext;
    private NFCOpenHelper mOpenHelper;
    private SQLiteDatabase db;

    public NFDataSource(Context context) {
        this.mContext = context;
    }

    public void open() throws SQLiteException {
        mOpenHelper = new NFCOpenHelper(mContext);
        db = mOpenHelper.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public void insertTag(Tag tag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NFContract.TagEntry.COLUMN_NAME, tag.getName());
        long _id = db.insert(NFContract.TagEntry.TABLE_NAME, null, contentValues);
    }

    public List<Tag> getTags() {
        Cursor tags = db.query(
                NFContract.TagEntry.TABLE_NAME,
                TagAdapter.TAG_COLUMNS,
                null,
                null,
                null,
                null,
                null
        );

        List<Tag> tagList = new ArrayList<>();
        while(tags.moveToNext()) {
            tagList.add(new Tag(tags.getString(TagAdapter.NAME_INDEX)));
        }

        tags.close();

        return tagList;
    }

    public void insertNestTask(String tagName, long targetValue) {
        ContentValues values = new ContentValues();
        values.put(NFContract.NestTaskEntry.COLUMN_TAG, tagName);
        values.put(NFContract.NestTaskEntry.COLUMN_TARGET_VALUE, targetValue);
        long id = db.insert(NFContract.NestTaskEntry.TABLE_NAME, null, values);
    }

    public List<NestTask> getNestTasks(Activity activity) {
        List<NestTask> tasks = new ArrayList<>();

        Cursor cursor = db.query(
                NFContract.NestTaskEntry.TABLE_NAME,
                new String[] {
                        NFContract.NestTaskEntry._ID,
                        NFContract.NestTaskEntry.COLUMN_TAG,
                        NFContract.NestTaskEntry.COLUMN_TARGET_VALUE
                },
                null,
                null,
                null,
                null,
                null
        );

        while(cursor.moveToNext()) {
            tasks.add(new NestTask(activity, cursor.getLong(2), true));
        }

        cursor.close();

        return tasks;
    }
}
