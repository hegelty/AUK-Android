package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
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
    public SoundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "alarm")
                .setContentTitle("AUK")
                .setContentText("알람 울리는 중")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 500};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));

        soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()).build();
        soundPool.load(getApplicationContext(), R.raw.alarm_sound, 0);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            streamIDs.add(soundPool.play(sampleId, 1, 1, 0, -1, 1));
            Log.d("playSound", streamIDs.toString() + "");
        });

        // stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        vibrator.cancel();
        if (soundPool != null) {
            for (int streamID : streamIDs) {
                soundPool.stop(streamID);
            }
            soundPool.release();
            soundPool = null;
        }
        super.onDestroy();
    }
}