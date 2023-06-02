package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.unrevr.munhaeryeok.MainActivity;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class SoundService extends Service {
    SoundPool soundPool;
    ArrayList<Integer> streamIDs = new ArrayList<>();
    Vibrator vibrator;
    boolean sound, vibration;

    private BroadcastReceiver killReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            Log.d("killReceiver", "onReceive");
            stop();
            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(killReceiver, new IntentFilter("alarm_killed"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SoundService", "onStartCommand");

        sound = intent.getBooleanExtra("sound", false);
        vibration = intent.getBooleanExtra("vibration", false);

        if(vibration) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 1000, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }

        if(sound) {
            soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()).build();
            soundPool.load(getApplicationContext(), R.raw.alarm_sound, 0);

            soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                streamIDs.add(soundPool.play(sampleId, 1, 1, 0, -1, 1));
                Log.d("playSound", streamIDs.toString() + "");
            });
        }

        // stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void stop() {
        if(vibrator!=null) vibrator.cancel();
        if (soundPool != null) {
            for (int streamID : streamIDs) {
                soundPool.stop(streamID);
            }
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    public void onDestroy() {
        stop();
        unregisterReceiver(killReceiver);
        super.onDestroy();
    }
}