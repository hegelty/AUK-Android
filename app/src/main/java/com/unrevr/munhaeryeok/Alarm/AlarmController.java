package com.unrevr.munhaeryeok.Alarm;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.Calendar;


public class AlarmController {
    private Context context;
    SharedPreferences pref;

    public AlarmController(Context context) {
        this.context = context;
        this.pref  = context.getSharedPreferences("alarm", MODE_PRIVATE);
    }

    public void setAlarm(int d, int h, int m, int s, int id) {
        // id = 0 이면 새로운 알람 생성
        Intent intent = new Intent(context, AlarmReciver.class);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, s);

        if(id==0) id = createID();
        else { // 알람 재설정
            calendar.add(Calendar.DATE, 7);
            deleteAlarm(id);
        }

        intent.putExtra("id", id);
        intent.putExtra("time", h + ":" + m + ":" + s);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        saveAlarm(id, d + ":" + h + ":" + m + ":" + s);
    }

    void setAlarmAgain(int id) {
        String time_string = getAlarmTimeString(id);
        String[] time = time_string.split(":");
        setAlarm(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]), Integer.parseInt(time[3]), id);
    }

    int createID() {
        int id = pref.getInt("last_id", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("last_id", id + 1);
        editor.apply();
        return id;
    }

    void saveAlarm(int id, String t) {
        SharedPreferences.Editor editor = pref.edit();
        String original = pref.getString("alarm_list", "");
        editor.putString("alarm_list", original + id + "-" + t + "\n");
        editor.apply();
    }

    boolean deleteAlarm(int id) {
        SharedPreferences.Editor editor = pref.edit();
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        String new_list = "";
        for(String s : list) {
            if(Integer.parseInt(s.split("-")[0]) == id) continue;
            new_list += s + "\n";
        }
        editor.putString("alarm_list", new_list);
        editor.apply();

        String alarm_time = getAlarmTimeString(id);
        if(alarm_time == null) return false;

        Intent intent = new Intent(context, AlarmReciver.class);
        PendingIntent pendingIntent = // 등록했을 때의 인텐트랑 같아야 삭제됨
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        return true;
    }

    String getAlarmTimeString(int id) {
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        for(String s : list) {
            String[] time = s.split("-");
            if(Integer.parseInt(time[0]) == id) {
                return time[1];
            }
        }
        return null;
    }
}
