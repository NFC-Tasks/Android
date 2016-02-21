package com.tristanwiley.nfctasks;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nestlabs.sdk.GlobalUpdate;
import com.nestlabs.sdk.NestException;
import com.nestlabs.sdk.NestListener;
import com.nestlabs.sdk.NestToken;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    AlertDialog adActions;
    AlertDialog ad;
    private TextToSpeech myTTS;
    private TagAdapter mTagAdapter;
    private FirebaseRecyclerAdapter<Tag, TagHolder> mFirebaseAdapter;
    private Firebase mRef;
    private Query mTagRef;
    private NFDataSource mDataSource;
    private ListView mListView;
    private static final int AUTH_TOKEN_REQUEST_CODE = 123;
    private NfcAdapter mNfcAdapter;
    private String mTagName;
    public static final String ARG_TAG = "argTag";
    private List<NestTask> mNestTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataSource = new NFDataSource(this);
        mDataSource.open();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTagName = "";
        if(getIntent() != null) {
            mTagName = getIntent().getStringExtra(ARG_TAG);
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            finish();
        }

        handleIntent(getIntent());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showActions();
//                isTargetAvalible("003-08-0338");
                setAlarm("NFC", 1, 10);
            }
        });

        setupListView();

        // Get nest tasks for this tag
        mNestTasks = mDataSource.getNestTasks(this);
    }

    private void setupListView() {
        mListView = (ListView) findViewById(R.id.tag_list_view);
        mTagAdapter = new TagAdapter(this, mDataSource.getTags());
        mListView.setAdapter(mTagAdapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showAddNestTaskDialog(((Tag)mTagAdapter.getItem(position)).getName());
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        } else if (id == R.id.action_tag_write) {
//            startTagWriteActivity();
//            return true;
//        } else if (id == R.id.action_tag_read) {
//            startTagReadActivity();
//            return true;
//        } else if(id == R.id.action_run) {
//            runTests();
//            return true;
//        }

        if(id == R.id.action_target) {
            startTargetActivity();
            return true;
        } else if(id == R.id.action_tag_write) {
            startTagWriteActivity();
            return true;
        } else if(id == R.id.action_tag_read) {
            startTagReadActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMap(String destination){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + destination));
        startActivity(i);
    }

    public void isTargetAvalible(String productId){
        Ion.with(getApplicationContext())
                .load("https://api.target.com/products/v3/" + productId + "?id_type=dpci&key=J5PsS2XGuqCnkdQq0Let6RSfvU7oyPwF")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonObject mainObj = result.get("product_composite_response").getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
                        String name = mainObj.get("data_page_link").getAsString().split("/p/")[1].split("/-/")[0].replace("-", " ");
                        boolean avalible = mainObj.get("is_orderable").getAsBoolean();
                        if(avalible){

                        }else{

                        }
                        Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setAlarm(String name, int hour, int minute){
        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, name);
        i.putExtra(AlarmClock.EXTRA_HOUR, hour);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        startActivity(i);
    }

    public void showActions() {
        final String names[] = {"Send Text", "Access Bluetooth", "Turn on Music", "Call Contact", "Read Weather"};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.add_action, null);
        alertDialog.setView(convertView);
//        alertDialog.setTitle("");

        ListView lv = (ListView) convertView.findViewById(R.id.choicesList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 2) {
                    musicDialog();
                }
                Toast.makeText(getApplicationContext(), names[position], Toast.LENGTH_SHORT).show();
            }
        });
        lv.setAdapter(adapter);
        adActions = alertDialog.show();
    }

    public void musicDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View convertView = inflater.inflate(R.layout.play_song, null);
        alertDialog.setView(convertView);
        convertView.findViewById(R.id.openSong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });

        convertView.findViewById(R.id.addSongAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) convertView.findViewById(R.id.songPath);
                if (!et.getText().toString().equals("")) {
                    //TODO add to database and stuff
                } else {
                    Toast.makeText(getApplicationContext(), "Enter a path", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ad = alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                LayoutInflater inflater = getLayoutInflater();
                final View convertView = inflater.inflate(R.layout.play_song, null);
                EditText et = (EditText) convertView.findViewById(R.id.songPath);

                //the selected audio
                Uri uri = data.getData();

                //TODO add to database and list and stuff
                ad.dismiss();
                adActions.dismiss();
            } else {
                Log.wtf("OnActivityResult", "Request Code not okay :(");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

        for(NestTask t : mNestTasks) {
            t.reauthenticateFromIntent(data);
        }
    }

    private void startTagWriteActivity() {
        Intent tagWrite = new Intent(this, TagWriteActivity.class);
        startActivity(tagWrite);
    }

    private void startTagReadActivity() {
        Intent tagRead = new Intent(this, TagReadActivity.class);
        startActivity(tagRead);
    }

    private void startTargetActivity() {
        Intent target = new Intent(this, TargetActivity.class);
        startActivity(target);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();

        mTagAdapter = new TagAdapter(this, mDataSource.getTags());
        mListView.setAdapter(mTagAdapter);

        setupForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();

        mNfcAdapter.disableForegroundDispatch(this);
    }

    private void showAddNestTaskDialog(String tagName) {
        AddNestTaskDialog dialog = AddNestTaskDialog.NewInstance(tagName);
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void setupForegroundDispatch() {
        Intent intent = new Intent(this, MainActivity.class);
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
                //TODO:
                new NdefReaderTask().execute(tag);
            } else {
                Log.v(TAG, "Wrong mime type.");
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            android.nfc.Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
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

            Log.v(TAG, "Have: " + mNestTasks.size());
            for(NestTask t : mNestTasks) {
                t.run();
            }
        }
    }
}
