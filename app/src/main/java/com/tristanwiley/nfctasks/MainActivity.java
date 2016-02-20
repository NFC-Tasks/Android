package com.tristanwiley.nfctasks;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(getApplicationContext());
        Firebase ref = new Firebase("https://nfc-tasks.firebaseio.com/");

        List<Operation> tasks = new ArrayList<>();
        List<String> t = new ArrayList<>();
        t.add("TEMP_TO,65");
        t.add("DEVICE_NAME,Joe");
        tasks.add(new Operation("TYPE_NEST", t));
        Tag tag = new Tag("Adam's Dumb Tag", tasks);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActions();
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
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_tag_write) {
            startTagWriteActivity();
            return true;
        } else if(id == R.id.action_nest) {
            startNestActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AlertDialog adActions;
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

    AlertDialog ad;
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
                if(!et.getText().toString().equals("")){
                    //TODO add to database and stuff
                }else{
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

//                Log.wtf("PATH", uri.getPath());
//                String path = uri.getEncodedPath();
//
//                et.setText(path);
//                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
//                viewIntent.setDataAndType(uri, "audio/*");
//                startActivity(Intent.createChooser(viewIntent, null));

            }else{
                Log.wtf("OnActivityResult", "Request Code not okay :(");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void playMusic(String title, String artist) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, title);
//        intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
        intent.putExtra(SearchManager.QUERY, title);
//        intent.putExtra(SearchManager.QUERY, artist);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void startNestActivity() {
        Intent nest = new Intent(this, NestActivity.class);
        startActivity(nest);
    }

    private void startTagWriteActivity() {
        Intent tagWrite = new Intent(this, TagWriteActivity.class);
        startActivity(tagWrite);
    }
}
