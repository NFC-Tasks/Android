package com.tristanwiley.nfctasks.Utils;

import android.content.Context;

import com.koushikdutta.ion.Ion;

/**
 * Created by Tristan on 2/20/2016.
 */
public class Twilio {
    public void sendMessage(Context c, String to, String body) {
        String url = "http://tristanwiley.com/labs/temp/twilio.php?tonum=" + to + "&body=" + body;
        Ion.with(c)
                .load(url.replace(" ", "%20"));
    }
}
