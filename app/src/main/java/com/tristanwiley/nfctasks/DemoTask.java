package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nestlabs.sdk.Callback;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Thermostat;
import com.tristanwiley.nfctasks.Utils.Twilio;

import java.util.HashMap;
import java.util.Locale;

/**
 * Task that runs everything for our demo.
 *
 * Created by adammcneilly on 2/21/16.
 */
public class DemoTask implements Task{
    private static final String TAG = NestTask.class.getSimpleName();
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Activity mActivity;
    private String mCity;
    private String mState;
    private long mTargetValue;
    private TextToSpeech mTTS;

    public DemoTask(Activity activity, String city, String state) {
        this.mActivity = activity;
        this.mCity = city;
        this.mState = state;

        setupNest();
    }

    @Override
    public void run() {
        String temp = "http://api.wunderground.com/api/eb509ff7b3f893bf/conditions/q/" + mState + "/" + mCity + ".json";
        Ion.with(mActivity.getApplicationContext())
                .load(temp.replace(" ", "%20"))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonObject current = result.get("current_observation").getAsJsonObject();
                        String weather = current.get("weather").getAsString();
                        String temp = current.get("temp_f").getAsString();
                        String feelsLike = current.get("feelslike_f").getAsString();

                        // If it is less than 40, set temp to 73
                        if(Double.parseDouble(temp) > 40.0) {
                            mTargetValue = 73;
                        } else {
                            mTargetValue = 70;
                        }

                        //TODO: Fix hack
                        if(mThermostat != null) {
                            // set value
                            String thermostatId = mThermostat.getDeviceId();
                            mNest.thermostats.setTargetTemperatureF(thermostatId, mTargetValue, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.v(TAG, "Success!");
                                }

                                @Override
                                public void onFailure(NestException exception) {
                                    Log.v(TAG, exception.getMessage());
                                }
                            });
                        } else {
                            Log.v(TAG, "Thermostat is null.");
                        }

                        final String speech = "It is currently " + temp + " degrees outside. I will set your thermostat to " + mTargetValue + ". I have sent a text to 315-572-9525. I will now begin navigation home, have a safe drive.";

                        Twilio twilio = new Twilio();
                        twilio.sendMessage(mActivity, "13155729525", "Testing!");

                        Log.wtf("sayWeather", speech);
                        // speak straight away
                        mTTS = new TextToSpeech(mActivity.getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(mTTS != null)
                                {
                                    mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                        @Override
                                        public void onStart(String utteranceId) {
                                            Log.v(TAG, utteranceId);
                                        }

                                        @Override
                                        public void onDone(String utteranceId) {
                                            Log.v(TAG, utteranceId);
                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=Rochester+MI"));
                                            mActivity.startActivity(i);
                                        }

                                        @Override
                                        public void onError(String utteranceId) {
                                            Log.v(TAG, utteranceId);
                                        }
                                    });

                                    Log.wtf("sayWeather", "Not null");
                                    mTTS.setSpeechRate(0.9f);
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                                    mTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, map);
                                }else{
                                    Log.wtf("sayWeather", "Totally null");
                                }
                            }


                        });

                        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                Log.v(TAG, utteranceId);
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                Log.v(TAG, utteranceId);

                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.v(TAG, utteranceId);
                            }
                        });
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
        return "DO ALL THE THINGS!";
    }
}