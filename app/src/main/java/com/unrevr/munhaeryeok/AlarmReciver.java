package com.unrevr.munhaeryeok;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("디버그", "알람 받음");

        Intent alarm_indent = new Intent(context, AlarmService.class);
        alarm_indent.putExtra("text", "알람");
        alarm_indent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarm_indent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(alarm_indent);

        AlarmController alarmController = new AlarmController(context.getApplicationContext());
        int id = intent.getIntExtra("id", 0);
        if(id!=0) alarmController.setAlarmAgain(id);
    }
}
