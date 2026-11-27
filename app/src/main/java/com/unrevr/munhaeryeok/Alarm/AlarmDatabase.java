package com.unrevr.munhaeryeok.Alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class AlarmDatabase extends SQLiteOpenHelper {
    private static AlarmDatabase instance;
    private final Context context;

    static synchronized AlarmDatabase get(Context context) {
        if (instance == null) instance = new AlarmDatabase(context.getApplicationContext());
        return instance;
    }

    private AlarmDatabase(Context context) {
        super(context, "alarms.db", null, 1);
        this.context = context;
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER NOT NULL,"
                + " minute INTEGER NOT NULL, days INTEGER NOT NULL, sound INTEGER NOT NULL,"
                + " vibration INTEGER NOT NULL, name TEXT NOT NULL, problem INTEGER NOT NULL,"
                + " favorite INTEGER NOT NULL, due INTEGER NOT NULL DEFAULT 0, token INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE legacy_requests (code INTEGER PRIMARY KEY)");
        LegacyAlarms legacy = new LegacyAlarms(readFile("alarm_data_alarms_list"), readFile("alarm_alarm_list"));
        for (AlarmData group : legacy.groups) {
            ContentValues values = values(group);
            if (group.id != 0) values.put("id", group.id);
            db.insertOrThrow("alarms", null, values);
        }
        for (int code : legacy.requestCodes)
            db.execSQL("INSERT INTO legacy_requests(code) VALUES (?)", new Object[]{code});
    }

    private String readFile(String name) {
        File file = new File(context.getFilesDir(), name);
        if (!file.exists()) return "";
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("기존 알람을 읽을 수 없습니다.", e);
        }
    }

    static ContentValues values(AlarmData alarm) {
        ContentValues values = new ContentValues();
        values.put("hour",alarm.h);
        values.put("minute",alarm.m);
        values.put("days",alarm.days);
        values.put("sound",alarm.sound);
        values.put("vibration",alarm.vibration);
        values.put("name",alarm.name);
        values.put("problem",alarm.problemType);
        values.put("favorite",alarm.favorite);
        return values;
    }

    @Override public void onUpgrade(SQLiteDatabase db, int from, int to) {
        throw new IllegalStateException("Unknown alarm schema " + from);
    }
}
