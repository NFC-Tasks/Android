package com.tristanwiley.nfctasks;

import android.content.Context;
import android.content.SharedPreferences;

import com.nestlabs.sdk.NestToken;

/**
 * Settings for NEST API.
 * <p/>
 * Created by adammcneilly on 2/20/16.
 */
public class Settings {
    private static final String TOKEN_KEY = "token";
    private static final String EXPIRATION_KEY = "expiration";

    public static void saveAuthToken(Context context, NestToken token) {
        if (token == null) {
            getPrefs(context).edit().remove(TOKEN_KEY).remove(EXPIRATION_KEY).commit();
            return;
        }
        getPrefs(context).edit()
                .putString(TOKEN_KEY, token.getToken())
                .putLong(EXPIRATION_KEY, token.getExpiresIn())
                .apply();
    }

    public static NestToken loadAuthToken(Context context) {
        final SharedPreferences prefs = getPrefs(context);
        final String token = prefs.getString(TOKEN_KEY, null);
        final long expirationDate = prefs.getLong(EXPIRATION_KEY, -1);

        if (token == null || expirationDate == -1) {
            return null;
        }

        return new NestToken(token, expirationDate);
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(NestToken.class.getSimpleName(), 0);
    }
}
