package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"com.unrevr.auk.RING".equals(intent.getAction())) return;
        int id = intent.getIntExtra("id", 0);
        int token = intent.getIntExtra("alarm_id", 0);
        // Claim this delivery and install the next alarm before relying on the UI.
        if (!new AlarmController(context).consumeOccurrence(id, token)) return;
        RingSession.enqueue(context, intent);
        try {
            context.startForegroundService(new Intent(context, AlarmService.class));
        } catch (SecurityException | IllegalStateException e) {
            RingSession.end(context, RingSession.key(intent));
            Log.w("AlarmReceiver", "Alarm service could not start", e);
        }
    }
}
