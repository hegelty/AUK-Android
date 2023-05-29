package com.unrevr.munhaeryeok.Alarm;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.unrevr.munhaeryeok.DataController;

import java.util.Calendar;


public class AlarmController {
    private Context context;
    DataController dataCon;

    public AlarmController(Context context) {
        this.context = context;
        this.dataCon  = new DataController(context, "alarm");
    }

    public int setAlarm(int d, int h, int m, int s, int id, boolean sound, boolean vibration, String name, int problem_type, boolean favorite) {
        // id = 0 이면 새로운 알람 생성
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, s);

        if(id==0) {
            id = createID();
        }
        else { // 알람 재설정
            deleteAlarm(id);
        }
        if(calendar.compareTo(Calendar.getInstance()) < 0) calendar.add(Calendar.DATE, 7);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm", id);
        intent.putExtra("h", h);
        intent.putExtra("m", m);
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

    int setAlarmAgain(int id) {
        Alarm alarm = getAlarm(id);
        return setAlarm(alarm.d, alarm.h, alarm.m, alarm.s, id, alarm.sound, alarm.vibration, alarm.name, alarm.problem_type, alarm.favorite);
    }

    public int reloadAlarms(int id) {
        Alarm alarm = getAlarm(id);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = // 등록했을 때의 인텐트랑 같아야 삭제됨
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
        Log.d("reloadAlarms", "reloadAlarms: " + id);
        return setAlarm(alarm.d, alarm.h, alarm.m, alarm.s, id, alarm.sound, alarm.vibration, alarm.name, alarm.problem_type, alarm.favorite);
    }

    int createID() {
        int id = dataCon.getInt("last_id", 0);
        dataCon.putInt("last_id", id + 1);
        return Integer.parseInt((id + 1) + Long.toString(System.currentTimeMillis()).substring(8));
    }

    void saveAlarm(Alarm alarm) {
        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("saveAlarm", "saveAlarm Original: " + original + alarm.toString() + "=");
        dataCon.putString("alarm_list", original + alarm.toString() + "=");
    }

    public boolean deleteAlarm(int id) {
        Log.d("deleteAlarm", "deleteAlarm: " + id);
        
        Alarm alarm = getAlarm(id); // 이게 앞에 있어야 dataCon에서 지워도 문제 없음

        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("deleteAlarm", "deleteAlarm Original: " + original);
        String[] list = original.split("=");
        String new_list = "";
        for(String s : list) {
            if(Integer.parseInt(s.split("-")[0]) == id) continue;
            if(s!=null) new_list += s + "=";
        }
        dataCon.putString("alarm_list", new_list);

        Log.d("deleteAlarm", "deleteAlarm: " + alarm.toString());

        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = // 등록했을 때의 인텐트랑 같아야 삭제됨
                PendingIntent.getBroadcast(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if(pendingIntent==null) return true;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        return true;
    }

    public Alarm getAlarm(int id) {
        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("getAlarm", "getAlarm: " + original);
        String[] list = original.split("=");
        for(String s : list) {
            s= s.trim();
            String[] time = s.split("-");
            if(Integer.parseInt(time[0]) == id) {
                return new Alarm(s);
            }
        }
        return null;
    }

    public int getNearestAlarmId() {
        String original = dataCon.getString("alarm_list", "").trim();
        if(original.equals("")) return 0;
        Log.d("getNearestAlarm", "getNearestAlarm: " + original);
        String[] list = original.split("=");
        String nearest = "";
        Calendar nearestCal = null;
        for(String s : list) {
            s= s.trim();
            String[] time = s.split("-")[1].split(":");
            int d = Integer.parseInt(time[0]);
            int h = Integer.parseInt(time[1]);
            int m = Integer.parseInt(time[2]);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, d);
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            calendar.set(Calendar.SECOND, 0);
            if(calendar.compareTo(Calendar.getInstance()) < 0) calendar.add(Calendar.DATE, 7);
            Log.d("getNearestAlarm", "getNearestAlarm: " + calendar.get(Calendar.DAY_OF_WEEK) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND));
            if(nearestCal == null || calendar.compareTo(nearestCal) < 0) {
                nearest = s;
                nearestCal = calendar;
            }
        }
        Log.d("getNearestAlarm", "NearestAlarm: " + nearest);
        try {
            return Integer.parseInt(nearest.split("-")[0]);
        } catch (Exception e) {
            return 0;
        }
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
        String[] tt = time.split(":");
        this.d = Integer.parseInt(tt[0]);
        this.h = Integer.parseInt(tt[1]);
        this.m = Integer.parseInt(tt[2]);
        this.s = Integer.parseInt(tt[3]);
        String[] ttt = t[2].split("\\|");
        this.name = ttt[0];
        this.sound = Integer.parseInt(ttt[1]) == 1;
        this.vibration = Integer.parseInt(ttt[2]) == 1;
        this.problem_type = Integer.parseInt(ttt[3]);
        this.favorite = Integer.parseInt(ttt[4]) == 1;
    }

    public String toString() {
        return id + "-" + time + "-" + name + "|" + (sound ? "1" : "0") + "|" + (vibration ? "1" : "0") + "|" + problem_type + "|" + (favorite ? "1" : "0");
    }
}
