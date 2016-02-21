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

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    AlertDialog adActions;
    AlertDialog ad;
    private TextToSpeech myTTS;
    private TaskAdapter mTaskAdapter;
    private FirebaseRecyclerAdapter<Tag, TagHolder> mFirebaseAdapter;
    private Firebase mRef;
    private Query mTagRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRef = new Firebase("https://nfc-tasks.firebaseio.com/tags");

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
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.tag_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        // mTaskAdapter = new TaskAdapter(this, getTasks());
        // recyclerView.setAdapter(mTaskAdapter);

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Tag, TagHolder>(Tag.class, R.layout.list_item_tag, TagHolder.class, mRef) {
            @Override
            protected void populateViewHolder(TagHolder tagHolder, Tag tag, int i) {
                tagHolder.bindTag(tag);
            }
        };

        recyclerView.setAdapter(mFirebaseAdapter);
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new NestTask(this, 65, true));
        tasks.add(new MusicTask(this, "Ass Back Home"));
        tasks.add(new WeatherTask(this, "Ann Arbor", "Michigan"));

        return tasks;
    }

    private void runTests() {
        for(Task task : mTaskAdapter.getTasks()) {
            task.run();
        }
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

    private void startTagWriteActivity() {
        Intent tagWrite = new Intent(this, TagWriteActivity.class);
        startActivity(tagWrite);
    }

    private void startTagReadActivity() {
        Intent tagRead = new Intent(this, TagReadActivity.class);
        startActivity(tagRead);
    }
}
