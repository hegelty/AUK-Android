package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.unrevr.munhaeryeok.DataController;
import com.unrevr.munhaeryeok.R;

public class RestartAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent reload_intent = new Intent(context, RestartAlarmService.class);
            context.startService(reload_intent);
        }
    }
}
