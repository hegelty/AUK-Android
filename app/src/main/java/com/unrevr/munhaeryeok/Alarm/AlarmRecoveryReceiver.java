package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;

import com.unrevr.munhaeryeok.UserInfo;

public class AlarmRecoveryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Only system recovery events reach this private manifest receiver.
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
            RingSession.state(context).edit().clear().apply();
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && context.getSystemService(UserManager.class).isUserUnlocked()) {
            // Reconcile answers recorded while credential-protected storage was locked.
            UserInfo.getInstance(context);
        }
        new AlarmController(context).reloadAlarms();
    }
}
