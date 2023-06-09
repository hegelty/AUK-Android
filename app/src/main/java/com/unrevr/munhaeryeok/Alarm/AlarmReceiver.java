package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        }
        else {
            int id = intent.getIntExtra("id", 0);
            int alarm_id = intent.getIntExtra("alarm_id", 0);
            int h = intent.getIntExtra("h", 0);
            int m = intent.getIntExtra("m", 0);
            boolean sound = intent.getBooleanExtra("sound", false);
            boolean vibration = intent.getBooleanExtra("vibration", false);
            String name = intent.getStringExtra("name");
            int problem_type = intent.getIntExtra("problem_type", 0);

            Log.d("AlarmReceiver", "id: " + id + ", alarm_id: " + alarm_id + ", h: " + h + ", m: " + m + ", sound: " + sound + ", vibration: " + vibration + ", name: " + name + ", problem_type: " + problem_type);
            Intent alarm_indent = new Intent(context, AlarmService.class);
            alarm_indent.putExtra("id", id);
            alarm_indent.putExtra("alarm_id", alarm_id);
            alarm_indent.putExtra("h", h);
            alarm_indent.putExtra("m", m);
            alarm_indent.putExtra("sound", sound);
            alarm_indent.putExtra("vibration", vibration);
            alarm_indent.putExtra("name", name);
            alarm_indent.putExtra("problem_type", problem_type);

            alarm_indent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            alarm_indent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(alarm_indent);
        }
    }
}
