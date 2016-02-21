package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.provider.MediaStore;

/**
 * Plays music.
 *
 * Created by adammcneilly on 2/20/16.
 */
public class MusicTask implements Task {
    private String mTitle;
    private Activity mActivity;

    public MusicTask(Activity activity, String title) {
        this.mActivity = activity;
        this.mTitle = title;
    }

    @Override
    public void run() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, mTitle);
        intent.putExtra(SearchManager.QUERY, mTitle);
        if (intent.resolveActivity(mActivity.getPackageManager()) != null) {
            mActivity.startActivity(intent);
        }
    }

    @Override
    public String toString() {
        return String.format("Play %s radio.", mTitle);
    }
}
