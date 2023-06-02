package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.unrevr.munhaeryeok.MainActivity;
import com.unrevr.munhaeryeok.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmSoundService extends Service {
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            return START_STICKY;
        }
        Log.d("AlarmSoundService", "onStartCommand: ");

        new Thread(new Runnable() {
            @Override
            public void run() {
                func();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    void func() {
        Intent intent = new Intent(getApplicationContext(), SoundService.class);
        startService(intent);
        // stopSelf();
    }

    private String getTime() {
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}