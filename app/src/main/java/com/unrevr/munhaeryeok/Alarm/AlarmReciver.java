package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String memo = intent.getStringExtra("memo");

        Log.d("AlarmReciver", "onReceive: " + id);

        Intent alarm_indent = new Intent(context, AlarmService.class);
        alarm_indent.putExtra("text", memo);
        alarm_indent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarm_indent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(alarm_indent);

        AlarmController alarmController = new AlarmController(context.getApplicationContext());
        if(id!=0) alarmController.setAlarmAgain(id);
    }
}
