package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmRecoveryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Only system recovery events reach this private manifest receiver.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            RingSession.state(context).edit().clear().apply();
        }
        new AlarmController(context).reloadAlarms();
    }
}
