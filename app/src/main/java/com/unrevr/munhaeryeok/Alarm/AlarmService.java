package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.net.Uri;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.AlarmGrades;

import java.util.concurrent.TimeUnit;

public class AlarmService extends Service {
    // A new channel is necessary because Android preserves the old channel's default importance.
    public static final String CHANNEL_ID = AlarmController.RING_CHANNEL;
    private static final long MAX_RING_MS = TimeUnit.MINUTES.toMillis(10);
    private static final AudioAttributes ALARM_AUDIO = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    private String playing;
    private final Runnable timeout = () -> {
        RingSession.end(this, playing);
        ringNext();
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notifications = getSystemService(NotificationManager.class);
        if (playing == null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.ringing_channel), NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            channel.enableVibration(false);
            notifications.createNotificationChannel(channel);

            // Even an expired/revoked delivery must satisfy startForegroundService's deadline.
            Notification.Builder starting = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getString(R.string.ringing_channel));
            if (Build.VERSION.SDK_INT >= 31) starting.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_DEFERRED);
            try {
                startForeground(-1, starting.build());
            } catch (SecurityException | IllegalStateException e) {
                Log.w("AlarmService", "Foreground alarm unavailable", e);
                RingSession.state(this).edit().clear().apply();
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return ringNext() ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    private boolean ringNext() {
        Intent intent = RingSession.nextUngraded(this, key -> AlarmGrades.isGraded(this, key));
        String occurrence = RingSession.key(intent);
        if (occurrence == null) {
            releaseOutputs();
            stopSelf();
            return false;
        }
        if (occurrence.equals(playing)) return true;
        releaseOutputs();
        NotificationManager notifications = getSystemService(NotificationManager.class);
        long elapsed = SystemClock.elapsedRealtime() - RingSession.state(this).getLong("started_at", 0);
        long remaining = MAX_RING_MS - elapsed;
        if (elapsed < 0 || remaining <= 0) {
            RingSession.end(this, occurrence);
            return ringNext();
        }
        if (!notifications.areNotificationsEnabled() ||
                notifications.getNotificationChannel(CHANNEL_ID).getImportance() == NotificationManager.IMPORTANCE_NONE) {
            RingSession.state(this).edit().clear().apply();
            stopSelf();
            return false;
        }

        try {
            startForeground(intent.getIntExtra("id", 0), ringingNotification(intent, occurrence, notifications));
        } catch (SecurityException | IllegalStateException e) {
            // Exact alarm access can be revoked between delivery and promotion.
            Log.w("AlarmService", "Foreground alarm unavailable", e);
            RingSession.state(this).edit().clear().apply();
            stopSelf();
            return false;
        }

        playing = occurrence;
        wakeLock = getSystemService(PowerManager.class).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AUK:ring");
        wakeLock.acquire(remaining);
        handler.postDelayed(timeout, remaining);
        if (intent.getBooleanExtra("vibration", false)) {
            vibrator = getSystemService(Vibrator.class);
            vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 500}, 0), ALARM_AUDIO);
        }
        if (intent.getBooleanExtra("sound", false)) {
            player = MediaPlayer.create(this, R.raw.alarm_sound, ALARM_AUDIO, AudioManager.AUDIO_SESSION_ID_GENERATE);
            if (player != null) {
                player.setLooping(true);
                player.start();
            }
        }
        return true;
    }

    private Notification ringingNotification(Intent ring, String occurrence, NotificationManager notifications) {
        Intent open = new Intent(ring).setClass(this, AlarmActivity.class);
        open.setData(Uri.parse("auk://ring/" + occurrence));
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent show = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        String name = ring.getStringExtra("name");
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(name == null || name.isEmpty() ? getString(R.string.ringing_channel) : name)
                .setContentText(getString(R.string.ringing_open_quiz))
                .setCategory(Notification.CATEGORY_ALARM)
                .setContentIntent(show)
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= 31) builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        if (Build.VERSION.SDK_INT < 34 || notifications.canUseFullScreenIntent()) {
            builder.setFullScreenIntent(show, true);
        }
        return builder.build();
    }

    private void releaseOutputs() {
        handler.removeCallbacks(timeout);
        if (player != null) {
            player.release();
            player = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        wakeLock = null;
        playing = null;
    }

    @Override
    public void onDestroy() {
        releaseOutputs();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
}
