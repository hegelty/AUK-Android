package com.unrevr.munhaeryeok;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;


public class AlarmController {
    private Context context;

    public AlarmController() {
        this.context = context.getApplicationContext();
    }

    void setAlarm(int d, int h, int m, int s) {
        Log.d("디버그", "알람 설정");
        Intent intent = new Intent(context, AlarmReciver.class);
        int id = createID();
        intent.putExtra("id", id);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, s);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        saveAlarm(id, d + ":" + h + ":" + m + ":" + s);

        Log.d("디버그", "알람 설정 완료");
    }

    void setAlarmAgain(int id) {
        String time_string = getAlarmTimeString(id);
        String[] time = time_string.split(":");
        setAlarm(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]), Integer.parseInt(time[3]));
    }

    int createID() {
        SharedPreferences pref = context.getSharedPreferences("alarm", MODE_PRIVATE);
        int id = pref.getInt("last_id", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("last_id", id + 1);
        editor.apply();
        return id;
    }

    void saveAlarm(int id, String t) {
        SharedPreferences pref = context.getSharedPreferences("alarm", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String original = pref.getString("alarm_list", "");
        editor.putString("alarm_list", original + id + "-" + t + "\n");
        editor.apply();
    }

    void deleteAlarm(int id) {
        SharedPreferences pref = context.getSharedPreferences("alarm", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        String new_list = "";
        for(String s : list) {
            if(s.split(":")[0].equals(id)) continue;
            new_list += s + "\n";
        }
        editor.putString("alarm_list", new_list);
        editor.apply();

        String alarm_time = getAlarmTimeString(id);
        if(alarm_time == null) return;
        String[] time = alarm_time.split(":");
        int d = Integer.parseInt(time[0]);
        int h = Integer.parseInt(time[1]);
        int m = Integer.parseInt(time[2]);
        int s = Integer.parseInt(time[3]);

        Intent intent = new Intent(context, AlarmReciver.class);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    String getAlarmTimeString(int id) {
        SharedPreferences pref = context.getSharedPreferences("alarm", MODE_PRIVATE);
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        for(String s : list) {
            if(s.split("-")[0].equals(id)) return s.split("-")[1];
        }
        return null;
    }
}
