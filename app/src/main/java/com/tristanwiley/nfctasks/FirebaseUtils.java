package com.tristanwiley.nfctasks;

import com.firebase.client.Firebase;

/**
 * Connects to firebase.
 *
 * Created by adammcneilly on 2/21/16.
 */
public class FirebaseUtils {
    public static void insertWeatherTask(String tagName, String city, String state, boolean set_thermostat) {
        Firebase tagRef = new Firebase(Constants.FIREBASE_URL).child("tags").child(tagName).child("weather_task");
        tagRef.child("city").setValue(city);
        tagRef.child("state").setValue(state);
    }

    public static void insertSetThermostatTask(String tagName, long targetValue, boolean isFahrenheit) {
        Firebase tagRef = new Firebase(Constants.FIREBASE_URL).child("tags").child(tagName).child("thermostat_task");
        tagRef.child("target_value").setValue(targetValue);
        tagRef.child("is_fahrenheit").setValue(isFahrenheit);
    }
}
