package com.tristanwiley.nfctasks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Speaks the weather to the user.
 *
 * Created by adammcneilly on 2/21/16.
 */
public class WeatherTask implements Task{
    private TextToSpeech mTTS;
    private String mCity;
    private String mState;
    private Activity mActivity;

    public WeatherTask(Activity activity, String city, String state) {
        mActivity = activity;
        mCity = city;
        mState = state;
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
                        final String finalSpeach = "It is currently " + weather + " outside.  It is " + temp + " degrees out and it feels like " + feelsLike + " degrees.";

                        Log.wtf("sayWeather", finalSpeach);
                        // speak straight away
                        mTTS = new TextToSpeech(mActivity.getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(mTTS != null)
                                {
                                    Log.wtf("sayWeather", "Not null");
                                    mTTS.speak(finalSpeach, TextToSpeech.QUEUE_FLUSH, null);
                                }else{
                                    Log.wtf("sayWeather", "Totally null");
                                }
                            }
                        });


                    }
                });
    }

    @Override
    public String toString() {
        return String.format("Giving weather for %s, %s", mCity, mState);
    }
}
