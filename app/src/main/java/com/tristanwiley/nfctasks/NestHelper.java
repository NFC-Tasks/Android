package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Thermostat;

/**
 * Reads and writes to the nest thermostat.
 *
 * Created by adammcneilly on 2/20/16.
 */
public class NestHelper {
    private static final String TAG = NestHelper.class.getSimpleName();
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Activity mActivity;

    public NestHelper(Activity activity) {
        this.mActivity = activity;

        NestAPI.setAndroidContext(mActivity);
        mNest = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(mActivity);

        if(mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(Constants.NEST_CLIENT_ID, Constants.NEST_CLIENT_SECRET, Constants.REDIRECT_URL);
        }
    }

    /**
     * Listens to get the first thermostat
     */
    private void fetchThermostat() {
        mNest.addGlobalListener(new NestListener.GlobalListener() {
            @Override
            public void onUpdate(@NonNull GlobalUpdate update) {
                // Get first thermostat
                mThermostat = update.getThermostats().get(0);
                if(mThermostat != null) {
                    Log.v(TAG, mThermostat.toString());
                }
            }
        });
    }

    /**
     * Authenticate with the Nest API and start listening for updates.
     *
     * @param token the token used to authenticate.
     */
    private void authenticate(NestToken token) {
        mNest.authWithToken(token, new NestListener.AuthListener() {

            @Override
            public void onAuthSuccess() {
                Log.v(TAG, "Authentication succeeded.");
                fetchThermostat();
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                Settings.saveAuthToken(mActivity, null);
                mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                Settings.saveAuthToken(mActivity, null);
                mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }
}
