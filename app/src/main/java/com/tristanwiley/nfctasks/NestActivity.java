package com.tristanwiley.nfctasks;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Structure;
import com.nestlabs.sdk.Thermostat;

public class NestActivity extends AppCompatActivity {
    private static final String TAG = NestActivity.class.getSimpleName();
    private static final String ARG_THERMOSTAT = "thermostatArg";
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private Structure mStructure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nest);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NestAPI.setAndroidContext(this);
        mNest = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(this);

        if (mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(Constants.CLIENT_ID, Constants.CLIENT_SECRET, Constants.REDIRECT_URL);
            mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
        }

        if (savedInstanceState != null) {
            mThermostat = savedInstanceState.getParcelable(ARG_THERMOSTAT);
        }

        Log.v(TAG, "Started!");

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, mThermostat.toString());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_THERMOSTAT, mThermostat);
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
                Log.v(TAG, mThermostat.toString());
            }

            @Override
            public void onAuthFailure(NestException exception) {
                Log.e(TAG, "Authentication failed with error: " + exception.getMessage());
                // Settings.saveAuthToken(mActivity, null);
                // mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }

            @Override
            public void onAuthRevoked() {
                Log.e(TAG, "Auth token was revoked!");
                // Settings.saveAuthToken(mActivity, null);
                // mNest.launchAuthFlow(mActivity, AUTH_TOKEN_REQUEST_CODE);
            }
        });
    }
}
