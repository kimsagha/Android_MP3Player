package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private MusicService.MyBinder myService = null;
    TextView tv;
    Button pauseButton;
    ListView lv;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.textView);
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        // connect to a service
        bindService(new Intent(MainActivity.this, MusicService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        lv = (ListView) findViewById(R.id.listView);
        // query the sd card
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0", null, null);
        lv.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, new String[] {MediaStore.Audio.Media.DATA}, new int[] {android.R.id.text1}));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                position = myItemInt;
                Cursor c = (Cursor) lv.getItemAtPosition(myItemInt);
                String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                Bundle bundle = new Bundle();
                bundle.putString("uri", uri);

                intent.putExtras(bundle);
                startService(intent);
            }
        });
    }

    // communicate with the service through callbacks
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceConnected");
            myService = (MusicService.MyBinder) service;
            myService.registerCallback(callback);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.d("g53mdp", "MainActivity onServiceDisconnected");
            myService.unregisterCallback(callback);
            myService = null;
        }
    };


    ICallback callback = new ICallback() {
        @Override
        public void mp3PlayerEvent(int progressMin, int progressSec, int durationMin,
                                   int durationSec) {
            runOnUiThread(new Runnable() {
                // updates as the service is running to show the user the progress
                @Override
                public void run() {
                    tv.setText(progressMin + " min : " + progressSec + " sec / " + durationMin +
                            " min : " + durationSec + " sec");
                }
            });
        }
    };

    // click to pause --> button will say resume --> click again to resume
    public void onClickPause(View v) {
        pauseButton = (Button) findViewById(R.id.buttonPause);
        if(myService!=null) {
            if (pauseButton.getText() == "RESUME") {
                pauseButton.setText("PAUSE");
                Cursor c = (Cursor) lv.getItemAtPosition(position);
                String uri = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                Bundle bundle = new Bundle();
                bundle.putString("uri", uri);
                Intent resumeIntent = new Intent(MainActivity.this,
                        MusicService.class);
                resumeIntent.putExtras(bundle);
                startService(resumeIntent);
            } else {
                myService.pauseMusic();
                pauseButton.setText("RESUME");
            }
        }
    }

    public void onClickStop(View v) {
        if(myService!=null)
            myService.stopMusic();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d("g53mdp", "MainActivity onDestroy");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d("g53mdp", "MainActivity onPause");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d("g53mdp", "MainActivity onResume");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Log.d("g53mdp", "MainActivity onStart");
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d("g53mdp", "MainActivity onStop");
    }
}