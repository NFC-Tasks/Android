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
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.IOException;

/**
 * Activity for writing information to an NFC tag.
 *
 * Created by adammcneilly on 2/20/16.
 */
public class TagWriteActivity extends AppCompatActivity{
    private boolean mWriteMode = false;
    private NfcAdapter mNFCAdapter;
    private PendingIntent mNFCPendingIntent;

    private String mMimeType = "application/tristanwiley.nfctasks";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_write);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Start listening
        mNFCAdapter = NfcAdapter.getDefaultAdapter(this);
        mNFCPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, TagWriteActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableTagWriteMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNFCAdapter.disableForegroundDispatch(this);
    }

    /**
     * Begins tag write mode and waits for an NFC tag to be detected.
     */
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNFCAdapter.enableForegroundDispatch(this, mNFCPendingIntent, mWriteTagFilters, null);
    }

    /**
     * Called when a new tag is detected.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        // If we are in tag writing mode and a tag is detected, write to it.
        if(mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime(mMimeType, getFlagString().getBytes());
            NdefMessage message = new NdefMessage(record);

            if(writeTag(message, detectedTag)) {
                //TODO: Success!
                Toast.makeText(this, "SUCCESS!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Writes a message to a tag and returns true if successful.
     */
    private boolean writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {
                ndef.connect();
                if(!ndef.isWritable()) {
                    Toast.makeText(this, "Error: tag not writable!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(ndef.getMaxSize() < size) {
                    Toast.makeText(this, "Error: tag too small.", Toast.LENGTH_SHORT).show();;
                    return false;
                }

                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException ioe) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch(Exception e) {
            return false;
        }
    }

    private String getFlagString() {
        return "Test flag string.";
    }
}
