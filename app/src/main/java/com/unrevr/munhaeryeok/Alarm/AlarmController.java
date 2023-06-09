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

    public int setAlarm(Alarm alarm) {
        return setAlarm(alarm.d, alarm.h, alarm.m, alarm.s, alarm.id, alarm.sound, alarm.vibration, alarm.name, alarm.problem_type, alarm.favorite);
    }
    public int setAlarm(int d, int h, int m, int s, int id, boolean sound, boolean vibration, String name, int problem_type, boolean favorite) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, s);

        int alarm_id = createAlarmID();
        if(calendar.compareTo(Calendar.getInstance()) < 0) calendar.add(Calendar.DATE, 7);

        Log.d("setAlarm", "id: " + id + ", alarm_id: " + alarm_id + ", h: " + h + ", m: " + m + ", sound: " + sound + ", vibration: " + vibration + ", name: " + name + ", problem_type: " + problem_type);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("alarm_id", alarm_id);
        intent.putExtra("h", h);
        intent.putExtra("m", m);
        intent.putExtra("sound", sound);
        intent.putExtra("vibration", vibration);
        intent.putExtra("name", name);
        intent.putExtra("problem_type", problem_type);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        alarm_id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );

        Alarm alarm = new Alarm(id, d, h, m, s, sound, vibration, name, problem_type, favorite);
        alarm.setAlarmId(alarm_id);
        saveAlarmId(id, alarm_id);

        return id;
    }

    public void reloadAlarms() {
        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("reloadAlarms", original);
        String[] list = original.split("=");
        String new_list = "";
        for(String s : list) {
            try {
                Alarm alarm = new Alarm(s);
                if (alarm.alarm_id != 0) {
                    cancelAlarm(alarm.alarm_id);
                    alarm.setAlarmId(0);
                    new_list += alarm.toString() + "=";
                }
                else new_list += s + "=";
            } catch(Exception e) {
                new_list += s + "=";
            }
        }

        dataCon.putString("alarm_list", new_list);
        setNearestAlarm();
    }

    int createID() {
        int id = dataCon.getInt("last_id", 0);
        dataCon.putInt("last_id", id + 1);
        return Integer.parseInt((id + 1) + Long.toString(System.currentTimeMillis()).substring(8));
    }

    int createAlarmID() {
        int id = dataCon.getInt("last_alarm_id", 0);
        dataCon.putInt("last_alarm_id", id + 1);
        return Integer.parseInt((id + 1) + Long.toString(System.currentTimeMillis()).substring(8));
    }

    int saveAlarm(Alarm alarm) {
        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("saveAlarm", "saveAlarm Original: " + original + alarm.toString() + "=");
        dataCon.putString("alarm_list", original + alarm.toString() + "=");
        return alarm.id;
    }

    int saveAlarm(int d, int h, int m, int s, boolean sound, boolean vibration, String name, int problem_type, boolean favorite) {
        Alarm alarm = new Alarm(createID(), d, h, m, s, sound, vibration, name, problem_type, favorite);
        return saveAlarm(alarm);
    }

    void saveAlarmId(int id, int alarm_id) {
        String original = dataCon.getString("alarm_list", "").trim();
        String[] list = original.split("=");
        String new_list = "";
        for(String s : list) {
            s = s.trim();
            if(s.equals("")) continue;
            Alarm alarm = new Alarm(s);
            if(alarm.id == id) {
                alarm.setAlarmId(alarm_id);
                new_list += alarm.toString() + "=";
                continue;
            }
            new_list += s + "=";
        }
        dataCon.putString("alarm_list", new_list);
    }

    public boolean deleteAlarm(int id) {
        Log.d("deleteAlarm", "deleteAlarm: " + id);
        
        Alarm alarm = getAlarm(id); // 이게 앞에 있어야 dataCon에서 지워도 문제 없음

        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("deleteAlarm", "deleteAlarm Original: " + original);
        String[] list = original.split("=");
        String new_list = "";
        for(String s : list) {
            s = s.trim();
            if(s.equals("")) continue;
            if(Integer.parseInt(s.split("-")[0]) == id) continue;
            if(s!=null) new_list += s + "=";
        }
        dataCon.putString("alarm_list", new_list);

        Log.d("deleteAlarm", "deleteAlarm: " + alarm.toString());

        if(alarm.alarm_id != 0) cancelAlarm(alarm.alarm_id);
        return true;
    }

    public void cancelAlarm(int alarm_id) {
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = // 등록했을 때의 인텐트랑 같아야 삭제됨
                PendingIntent.getBroadcast(
                        context,
                        alarm_id,
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
    }

    public Alarm getAlarm(int id) {
        String original = dataCon.getString("alarm_list", "").trim();
        Log.d("getAlarm", "getAlarm: " + original);
        String[] list = original.split("=");
        for(String s : list) {
            s = s.trim();
            if(s.equals("")) continue;
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
        Log.d("getNearestAlarm", "getNearestAlarm original: " + original);
        String[] list = original.split("=");
        String nearest = "";
        Calendar nearestCal = null;
        for(String s : list) {
            s = s.trim();
            Log.d("getNearestAlarm", "getNearestAlarm: " + s);
            if(s.equals("")) continue;
            try {
                String[] time = s.split("-")[2].split(":");
                int d = Integer.parseInt(time[0]);
                int h = Integer.parseInt(time[1]);
                int m = Integer.parseInt(time[2]);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, d);
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, m);
                calendar.set(Calendar.SECOND, 0);
                if (calendar.compareTo(Calendar.getInstance()) < 0) calendar.add(Calendar.DATE, 7);
                Log.d("getNearestAlarm", "getNearestAlarm: " + calendar.get(Calendar.DAY_OF_WEEK) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND));
                if (nearestCal == null || calendar.compareTo(nearestCal) < 0) {
                    nearest = s;
                    nearestCal = calendar;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("getNearestAlarm", "NearestAlarm: " + nearest);
        try {
            return Integer.parseInt(nearest.split("-")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setNearestAlarm() {
        Alarm alarm = getAlarm(getNearestAlarmId());
        if(alarm == null) return;

        setAlarm(alarm);
    }
}

class Alarm {
    public int id, alarm_id = 0;
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
        Log.d("Alarm", "Alarm: " + s);
        String[] t = s.split("-");
        this.id = Integer.parseInt(t[0]);
        this.alarm_id = Integer.parseInt(t[1]);
        this.time = t[2];
        String[] tt = time.split(":");
        this.d = Integer.parseInt(tt[0]);
        this.h = Integer.parseInt(tt[1]);
        this.m = Integer.parseInt(tt[2]);
        this.s = Integer.parseInt(tt[3]);
        String[] ttt = t[3].split("\\|");
        this.name = ttt[0];
        this.sound = Integer.parseInt(ttt[1]) == 1;
        this.vibration = Integer.parseInt(ttt[2]) == 1;
        this.problem_type = Integer.parseInt(ttt[3]);
        this.favorite = Integer.parseInt(ttt[4]) == 1;
    }

    public void setAlarmId(int alarm_id) {
        this.alarm_id = alarm_id;
    }

    public String toString() {
        return id + "-" + alarm_id + "-" + time + "-" + name + "|" + (sound ? "1" : "0") + "|" + (vibration ? "1" : "0") + "|" + problem_type + "|" + (favorite ? "1" : "0");
    }
}
