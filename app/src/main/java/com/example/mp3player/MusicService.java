package com.example.mp3player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MusicService extends Service {

    RemoteCallbackList<MyBinder> remoteCallbackList = new RemoteCallbackList<MyBinder>();
    MP3Player mp3Player = new MP3Player();
    protected MusicPlayer musicPlayer;
    private final String CHANNEL_ID = "100";
    int NOTIFICATION_ID = 001;

    protected class MusicPlayer extends Thread implements Runnable {
        public boolean running = true;
        public int progressMin = 0;
        public int progressSec = 0;
        public int durationMin = 0;
        public int durationSec = 0;

        public MusicPlayer() {
            this.start();
        }

        public void run() {
            while(this.running) {

                try {Thread.sleep(2000);} catch(Exception e) {return;}

                // get progress values out of duration to show user
                progressMin = mp3Player.getProgress()/60000;
                progressSec = (mp3Player.getProgress()/1000) % 60;
                durationMin = mp3Player.getDuration()/60000;
                durationSec = (mp3Player.getDuration()/1000) % 60;
                doCallbacks(progressMin, progressSec, durationMin, durationSec);
                Log.d("g53mdp", "Service music player");
            }

            Log.d("g53mdp", "Music player thread exiting");
        }
    }

    // broadcast progress as service is running
    public void doCallbacks(int progressMin, int progressSec, int durationMin, int durationSec) {
        final int n = remoteCallbackList.beginBroadcast();
        for (int i=0; i<n; i++) {
            remoteCallbackList.getBroadcastItem(i).callback.mp3PlayerEvent(progressMin, progressSec, durationMin, durationSec);
        }
        remoteCallbackList.finishBroadcast();
    }

    // implement a portal to wrap up communication with the activity
    public class MyBinder extends Binder implements IInterface
    {
        @Override
        public IBinder asBinder() {
            return this;
        }

        void pauseMusic() {
            MusicService.this.pauseMusic();
        }

        void stopMusic() {
            MusicService.this.stopMusic();
        }

        public void registerCallback(ICallback callback) {
            this.callback = callback;
            remoteCallbackList.register(MyBinder.this);
        }

        public void unregisterCallback(ICallback callback) {
            remoteCallbackList.unregister(MyBinder.this);
        }

        ICallback callback;
    }

    public void pauseMusic() {
        mp3Player.pause();
    }

    // stop the music and cancel the notification
    public void stopMusic() {
        mp3Player.stop();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onCreate");
        musicPlayer = new MusicPlayer();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onBind");
        return new MyBinder();
    }

    // start the service and show the notification
    // click notification to come back to app if on another task
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onStartCommand");
        Bundle bundle = intent.getExtras();
        String uri = bundle.getString("uri");
        if (mp3Player.getState() == MP3Player.MP3PlayerState.PAUSED) {
            mp3Player.play();
        } else {
            mp3Player.stop();
            mp3Player.load(uri);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel name";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        // intent to start the MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent notificationIntent = new Intent(MusicService.this, MainActivity.class);
        // pending intent to be able to return to the app
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("MP3 Player")
                //.setContentText("")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("g53mdp", "service onUnbind");
        return super.onUnbind(intent);
    }

}
