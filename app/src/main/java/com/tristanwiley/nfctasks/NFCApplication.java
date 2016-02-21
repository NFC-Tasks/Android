package com.tristanwiley.nfctasks;

import android.app.Application;

import com.firebase.client.Firebase;
import com.nestlabs.sdk.NestAPI;

/**
 * Created by adammcneilly on 2/21/16.
 */
public class NFCApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        NestAPI.setAndroidContext(this);
        NestAPI.getInstance();
        //Firebase.setAndroidContext(this);
    }
}
