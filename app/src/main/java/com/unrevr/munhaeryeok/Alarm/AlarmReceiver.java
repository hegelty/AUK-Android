package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        }
        else {
            int id = intent.getIntExtra("id", 0);
            int h = intent.getIntExtra("h", 0);
            int m = intent.getIntExtra("m", 0);
            boolean sound = intent.getBooleanExtra("sound", false);
            boolean vibration = intent.getBooleanExtra("vibration", false);
            String name = intent.getStringExtra("name");
            int problem_type = intent.getIntExtra("problem_type", 0);


            Intent alarm_indent = new Intent(context, AlarmService.class);
            alarm_indent.putExtra("id", id);
            alarm_indent.putExtra("h", h);
            alarm_indent.putExtra("m", m);
            alarm_indent.putExtra("sound", sound);
            alarm_indent.putExtra("vibration", vibration);
            alarm_indent.putExtra("name", name);
            alarm_indent.putExtra("problem_type", problem_type);

            alarm_indent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            alarm_indent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(alarm_indent);

            AlarmController alarmController = new AlarmController(context.getApplicationContext());
            if (id != 0) alarmController.reloadAlarms();
        }
    }
}
