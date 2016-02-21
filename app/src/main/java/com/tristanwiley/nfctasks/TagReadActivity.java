package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestAPI;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;
import com.nestlabs.sdk.Thermostat;

import java.io.UnsupportedEncodingException;

public class TagReadActivity extends AppCompatActivity {
    private static final String TAG = TagReadActivity.class.getSimpleName();
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NfcAdapter mNfcAdapter;
    private NestAPI mNest;
    private NestToken mToken;
    private Thermostat mThermostat;
    private TextToSpeech mTTS;
    private NestTask mNestTask;
    private MusicTask mMusicTask;
    private DemoTask mDemoTask;
    private String mTagName;

    public static final String ARG_TAG = "argTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_read);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTagName = "";
        if(getIntent() != null) {
            mTagName = getIntent().getStringExtra(ARG_TAG);
        }

        TextView textView = (TextView) findViewById(R.id.tag_instructions);
        textView.setText("Place phone on the " + mTagName + " tag...");

        // Create nest task
        mNestTask = new NestTask(this, 65, true);
        mMusicTask = new MusicTask(this, "Never Gonna Give You Up");
        mDemoTask = new DemoTask(this, "Rochester", "Michigan");


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            finish();
        }

        handleIntent(getIntent());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupNest() {
        NestAPI.setAndroidContext(this);
        mNest = NestAPI.getInstance();
        mToken = Settings.loadAuthToken(this);

        if (mToken != null) {
            authenticate(mToken);
        } else {
            mNest.setConfig(Constants.NEST_CLIENT_ID, Constants.NEST_CLIENT_SECRET, Constants.REDIRECT_URL);
            mNest.launchAuthFlow(this, AUTH_TOKEN_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK || requestCode != AUTH_TOKEN_REQUEST_CODE) {
            Log.e(TAG, "Finished with no result.");
            return;
        }

        mNestTask.reauthenticateFromIntent(intent);
    }

    private void setupForegroundDispatch() {
        Intent intent = new Intent(this, TagReadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(Constants.MIME_TYPE);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.v(TAG, "Check your mime type.");
        }

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
    }

    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            if (Constants.MIME_TYPE.equals(intent.getType())) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.v(TAG, "Wrong mime type.");
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
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

    public void playMusic(String title, String artist) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, title);
        intent.putExtra(SearchManager.QUERY, title);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void sayWeather(String city, String state) {
        String temp = "http://api.wunderground.com/api/eb509ff7b3f893bf/conditions/q/" + state + "/" + city + ".json";
        Ion.with(getApplicationContext())
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
                        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (mTTS != null) {
                                    Log.wtf("sayWeather", "Not null");
                                    mTTS.speak(finalSpeach, TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    Log.wtf("sayWeather", "Totally null");
                                }
                            }
                        });


                    }
                });
    }

    /**
     * Authenticate with the Nest API and start listening for updates.
     *
     * @param token the token used to authenticate.
     */
    private void authenticate(NestToken token) {
        final Activity mActivity = this;

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

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                Log.v(TAG, "NDEF not supported.");
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException uee) {
                        Log.v(TAG, uee.getMessage());
                    }
                }
            }

            Log.v(TAG, "Missed if.");
            return null;
        }

        private String readText(NdefRecord ndefRecord) throws UnsupportedEncodingException {
            byte[] payload = ndefRecord.getPayload();

            String utf8 = "UTF-8";
            String utf16 = "UTF-16";
            String textEncoding = ((payload[0] & 128) == 0) ? utf8 : utf16;

            // int languageCodingLength = payload[0] & 0063;

            Log.v(TAG, textEncoding);
            return new String(payload, 0, payload.length, textEncoding);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.v(TAG, "onPostExecute");

            Log.v(TAG, "Read data: " + s);

            // If we read "nest", call thing
            if (s.equals("nest")) {
                mDemoTask.run();
                // Set temperature
                // mNestTask.run();

                // Say weather first
                // sayWeather("Ann Arbor", "Michigan");

                //NGGYU
                // Log.v(TAG, mMusicTask.toString());
                // mMusicTask.run();
            }
        }
    }
}
