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

    public int setAlarm(int d, int h, int m, int s, int id, boolean sound, boolean vibration, String name, int problem_type, boolean favorite) {
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
        intent.putExtra("sound", sound);
        intent.putExtra("vibration", vibration);
        intent.putExtra("name", name);
        intent.putExtra("problem_type", problem_type);
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

        Alarm alarm = new Alarm(id, d, h, m, s, sound, vibration, name, problem_type, favorite);
        saveAlarm(alarm);
        return id;
    }

    void setAlarmAgain(int id) {
        Alarm alarm = getAlarm(id);
        setAlarm(alarm.d, alarm.h, alarm.m, alarm.s, id, alarm.sound, alarm.vibration, alarm.name, alarm.problem_type, alarm.favorite);
    }

    int createID() {
        int id = pref.getInt("last_id", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("last_id", id + 1);
        editor.apply();
        return id;
    }

    void saveAlarm(Alarm alarm) {
        SharedPreferences.Editor editor = pref.edit();
        String original = pref.getString("alarm_list", "");
        editor.putString("alarm_list", original + alarm.toString() + "\n");
        editor.apply();
    }

    public boolean deleteAlarm(int id) {
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

    Alarm getAlarm(int id) {
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        for(String s : list) {
            String[] time = s.split("-");
            if(Integer.parseInt(time[0]) == id) {
                return new Alarm(s);
            }
        }
        return null;
    }

    Alarm[] getAlarmList() {
        String original = pref.getString("alarm_list", "");
        String[] list = original.split("\n");
        Alarm[] alarms = new Alarm[list.length];
        for(int i=0; i<list.length; i++) {
            alarms[i] = new Alarm(list[i]);
        }
        return alarms;
    }
}

class Alarm {
    public int id;
    public String time;
    public boolean sound;
    public boolean vibration;
    public int d, h, m, s;
    public String name;
    public int problem_type;
    public boolean favorite;

    public Alarm(int id, String time, boolean sound, boolean vibrate, String name, int problem_type, boolean favorite) {
        this.id = id;
        this.time = time;
        this.sound = sound;
        this.vibration = vibrate;
        this.name = name;
        this.problem_type = problem_type;
        this.favorite = favorite;
        String[] t = time.split(":");
        this.d = Integer.parseInt(t[0]);
        this.h = Integer.parseInt(t[1]);
        this.m = Integer.parseInt(t[2]);
        this.s = Integer.parseInt(t[3]);
    }

    public Alarm(int id, int d, int h, int m, int s, boolean sound, boolean vibrate, String name, int problem_type, boolean favorite) {
        this.id = id;
        this.d = d;
        this.h = h;
        this.m = m;
        this.s = s;
        this.sound = sound;
        this.vibration = vibrate;
        this.name = name;
        this.problem_type = problem_type;
        this.favorite = favorite;
        this.time = d + ":" + h + ":" + m + ":" + s;
    }

    public Alarm(String s) {
        String[] t = s.split("-");
        this.id = Integer.parseInt(t[0]);
        this.time = t[1];
        this.name = t[2];
        String[] tt = time.split(":");
        this.d = Integer.parseInt(tt[0]);
        this.h = Integer.parseInt(tt[1]);
        this.m = Integer.parseInt(tt[2]);
        this.s = Integer.parseInt(tt[3].split("/|")[0]);
        this.sound = Integer.parseInt(tt[3].split("/|")[1]) == 1;
        this.vibration =  Integer.parseInt(tt[3].split("/|")[2]) == 1;
        this.problem_type = Integer.parseInt(tt[3].split("/|")[3]);
        this.favorite = Integer.parseInt(tt[3].split("/|")[4]) == 1;
    }

    public String toString() {
        return id + "-" + time + "-" + name + "|" + (sound ? "1":"0") + "|" + (vibration ? "1":"0") + "|" + problem_type + "|" + (favorite ? "1":"0");
    }
}