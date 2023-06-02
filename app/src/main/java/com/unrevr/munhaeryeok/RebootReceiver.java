package com.unrevr.munhaeryeok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.unrevr.munhaeryeok.Alarm.RestartAlarmService;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Receiver", "onReceive: " + intent.getAction());
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d("Receiver", "BOOT_COMPLETED");
            Intent reload_intent = new Intent(context, RestartAlarmService.class);
            context.startForegroundService(reload_intent);
        }
    }
}