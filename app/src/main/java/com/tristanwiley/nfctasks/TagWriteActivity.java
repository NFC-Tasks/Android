package com.tristanwiley.nfctasks;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

public class TagWriteActivity extends AppCompatActivity {
    private static final String TAG = TagWriteActivity.class.getSimpleName();
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private boolean mWriteMode;
    private NFDataSource mDataSource;
    private EditText mTagNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_write);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTagNameEditText = (EditText) findViewById(R.id.tag_name);

        setupNfc();

        mDataSource = new NFDataSource(this);
        mDataSource.open();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, TagWriteActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableTagWriteMode();
        mDataSource.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        mDataSource.close();
    }

    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[]{tagDetected};
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime(Constants.MIME_TYPE, getTagString().getBytes());
            NdefMessage message = new NdefMessage(record);

            if (writeTag(message, detectedTag)) {
                // SUCCESS!
                Log.v(TAG, "Successfully wrote tag!");
                // Insert one with that message
                mDataSource.open();
                mDataSource.insertTag(new com.tristanwiley.nfctasks.Tag(getTagString()));
                finish();
            }
        }
    }

    private boolean writeTag(NdefMessage message, Tag tag) {
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Log.v(TAG, "Error: Tag not writable.");
                    return false;
                } else if (ndef.getMaxSize() < message.toByteArray().length) {
                    Log.v(TAG, "Error: Tag too small.");
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        ;
                        format.format(message);
                        return true;
                    } catch (IOException ioe) {
                        Log.v(TAG, ioe.getMessage());
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            return false;
        }
    }

    private String getTagString() {
        return mTagNameEditText.getText().toString();
    }
}
