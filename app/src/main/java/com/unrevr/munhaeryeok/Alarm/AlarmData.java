package com.unrevr.munhaeryeok.Alarm;

import android.util.Log;

import com.unrevr.munhaeryeok.DataController;

public class AlarmData {
    public int id;
    public int[] alarm_ids = {0, 0, 0, 0, 0, 0, 0};
    public int h;
    public int m;
    boolean sound;
    boolean vibration;
    public String name;
    int problem_type;
    public boolean favorite;

    AlarmData(int id, int h, int m, int[] alarm_ids, boolean sound, boolean vibration, String name, int problem_type, boolean favorite) {
        this.id = id;
        this.h = h;
        this.m = m;
        this.alarm_ids = alarm_ids;
        this.sound = sound;
        this.vibration = vibration;
        this.name = name;
        this.problem_type = problem_type;
        this.favorite = favorite;
    }

    public AlarmData(String s) {
        Log.d("debug_AlarmData", s);
        String[] list = s.split("\\|");
        id = Integer.parseInt(list[0]);
        String[] time = list[1].split(":");
        h = Integer.parseInt(time[0]);
        m = Integer.parseInt(time[1]);
        sound = Boolean.parseBoolean(list[2]);
        vibration = Boolean.parseBoolean(list[3]);
        name = list[4];
        problem_type = Integer.parseInt(list[5]);
        favorite = Boolean.parseBoolean(list[6]);
        String[] ids = list[7].split(",");
        for (int i = 0; i < 7; i++) {
            alarm_ids[i] = Integer.parseInt(ids[i]);
        }
    }

    public String toString() {
        String str = String.valueOf(id) + "|";
        str += h + ":" + m;
        str += "|" + sound + "|" + vibration + "|" + name + "|" + problem_type + "|" + favorite + "|";
        for (int i = 0; i < 7; i++) {
            str += alarm_ids[i] + ",";
        }
        return str;
    }
}
