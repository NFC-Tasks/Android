package com.tristanwiley.nfctasks;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class NFCApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
