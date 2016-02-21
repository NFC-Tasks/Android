package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Thermostat;

/**
 * Created by adammcneilly on 2/20/16.
 */
public class NestTask implements Task {
    private static final String TAG = NestTask.class.getSimpleName();
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Activity mActivity;
    private boolean mFahrenheit;
    private long mTargetValue;
    private TextToSpeech mTTS;

    public NestTask(Activity activity, long targetValue, boolean fahrenheit) {
        this.mActivity = activity;
        this.mTargetValue = targetValue;
        this.mFahrenheit = fahrenheit;

        setupNest();
    }

    @Override
    public void run() {

        // If thermostat is null just return
        if(mThermostat == null) {
            return;
        }

        // set value
        String thermostatId = mThermostat.getDeviceId();

        // Set value based on temp scale
        if (mFahrenheit) {
            mNest.thermostats.setTargetTemperatureF(thermostatId, mTargetValue);
        } else {
            mNest.thermostats.setTargetTemperatureC(thermostatId, mTargetValue);
        }

        say();
    }

    private void say() {
        mTTS = new TextToSpeech(mActivity.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(mTTS != null) {
                    String speech = "I will set your thermostat to " + mTargetValue + ".";
                    mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }

    public void reauthenticateFromIntent(Intent intent) {
        mToken = NestAPI.getAccessTokenFromIntent(intent);
        if (mToken != null) {
            Settings.saveAuthToken(mActivity, mToken);
            authenticate(mToken);
        } else {
            Log.e(TAG, "Unable to resolve access token from payload.");
        }
    }

    private void setupNest() {
        mNest = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(mActivity);

        if (mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(Constants.NEST_CLIENT_ID, Constants.NEST_CLIENT_SECRET, Constants.REDIRECT_URL);
            mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
        }

        fetchThermostat();
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
                if (mThermostat != null) {
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

    @Override
    public String toString() {
        return String.format("Setting Nest thermostat target temperature to %d%s", mTargetValue, (mFahrenheit ? "F" : "C"));
    }
}
