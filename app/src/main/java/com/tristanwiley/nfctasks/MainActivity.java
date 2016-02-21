package com.tristanwiley.nfctasks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    AlertDialog adActions;
    AlertDialog ad;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        new Twilio().sendMessage(getApplicationContext(), "5867442919", "Good shit fam");
//        sayWeather("Ann Arbor", "MI");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActions();
            }
        });

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.task_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        TaskAdapter taskAdapter = new TaskAdapter(this, getTasks());
        recyclerView.setAdapter(taskAdapter);

        ItemTouchHelper.Callback callback = new TaskTouchHelper(taskAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new NestTask(this, 65, true));
        tasks.add(new MusicTask(this, "Never Gonna Give You Up"));
        tasks.add(new WeatherTask(this, "Ann Arbor", "Michigan"));

        return tasks;
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
        } else if (id == R.id.action_tag_write) {
            startTagWriteActivity();
            return true;
        } else if (id == R.id.action_tag_read) {
            startTagReadActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                        myTTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if (myTTS != null) {
                                    Log.wtf("sayWeather", "Not null");
                                    myTTS.speak(finalSpeach, TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    Log.wtf("sayWeather", "Totally null");
                                }
                            }
                        });


                    }
                });
    }

    private void startTagWriteActivity() {
        Intent tagWrite = new Intent(this, TagWriteActivity.class);
        startActivity(tagWrite);
    }

    private void startTagReadActivity() {
        Intent tagRead = new Intent(this, TagReadActivity.class);
        startActivity(tagRead);
    }
}
