package com.tristanwiley.nfctasks;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.*;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class TargetActivity extends AppCompatActivity {
    private static final String TAG = TargetActivity.class.getSimpleName();
    private NfcAdapter mNfcAdapter;
    private TextToSpeech mTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    private void setupForegroundDispatch() {
        Intent intent = new Intent(this, TargetActivity.class);
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
                android.nfc.Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                // Tag found, update textview
                // mTextView.setText("Preparing tasks...");
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

    public void isTargetAvailable(String productId){
        Ion.with(getApplicationContext())
                .load("https://api.target.com/products/v3/" + productId + "?id_type=dpci&key=J5PsS2XGuqCnkdQq0Let6RSfvU7oyPwF")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonObject mainObj = result.get("product_composite_response").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
                        final String name = mainObj.get("data_page_link").getAsString().split("/p/")[1].split("/-/")[0].replace("-", " ");
                        final boolean available = mainObj.get("is_orderable").getAsBoolean();

                        mTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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

                                        }

                                        @Override
                                        public void onError(String utteranceId) {
                                            Log.v(TAG, utteranceId);
                                        }
                                    });

                                    Log.wtf("sayWeather", "Not null");
                                    String speech = name + " is " + (available ? "available" : "") + " at Target for ordering!";
                                    mTTS.setSpeechRate(0.8f);
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
                        Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class NdefReaderTask extends AsyncTask<android.nfc.Tag, Void, String> {
        @Override
        protected String doInBackground(android.nfc.Tag... params) {
            android.nfc.Tag tag = params[0];

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
                isTargetAvailable("003-08-0338");
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
