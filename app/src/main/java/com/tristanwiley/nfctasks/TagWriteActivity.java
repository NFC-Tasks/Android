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
import android.support.v7.app.AppCompatActivity;
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

    /**
     * Begins tag write mode and waits for an NFC tag to be detected.
     */
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNFCAdapter.enableForegroundDispatch(this, mNFCPendingIntent, mWriteTagFilters, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // If we are in tag writing mode and a tag is detected, write to it.
        if(mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NdefRecord record = NdefRecord.createMime(mMimeType, getFlagString().getBytes());
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });

            if(writeTag(message, detectedTag)) {
                //TODO: Success!
            }
        }
    }

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
