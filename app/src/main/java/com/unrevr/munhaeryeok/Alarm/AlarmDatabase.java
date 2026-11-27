package com.unrevr.munhaeryeok.Alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.UserManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class AlarmDatabase extends SQLiteOpenHelper {
    private static AlarmDatabase instance;
    private static final String DATABASE = "alarms.db";
    private final Context credentialContext;
    private boolean migratedWhileUnlocked;

    static synchronized AlarmDatabase get(Context context) {
        if (instance == null) instance = new AlarmDatabase(context.getApplicationContext());
        instance.importAfterUnlock();
        return instance;
    }

    private AlarmDatabase(Context context) {
        super(context.createDeviceProtectedStorageContext(), DATABASE, null, 1);
        credentialContext = context;
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms (id INTEGER PRIMARY KEY AUTOINCREMENT, hour INTEGER NOT NULL,"
                + " minute INTEGER NOT NULL, days INTEGER NOT NULL, sound INTEGER NOT NULL,"
                + " vibration INTEGER NOT NULL, name TEXT NOT NULL, problem INTEGER NOT NULL,"
                + " favorite INTEGER NOT NULL, due INTEGER NOT NULL DEFAULT 0, token INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE legacy_requests (code INTEGER PRIMARY KEY)");
        db.execSQL("CREATE TABLE import_state (source TEXT PRIMARY KEY)");
    }

    // The scheduler can be opened at LOCKED_BOOT_COMPLETED before credential storage is available.
    // Import only after the first unlock, even when the device database already exists.
    private void importAfterUnlock() {
        if (migratedWhileUnlocked || !credentialContext.getSystemService(UserManager.class).isUserUnlocked()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM import_state WHERE source='credential'", null) == 0) {
                File oldFile = credentialContext.getDatabasePath(DATABASE);
                if (oldFile.exists()) {
                    try (SQLiteDatabase old = SQLiteDatabase.openDatabase(oldFile.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
                        copyRows(old, db, "alarms");
                        copyRows(old, db, "legacy_requests");
                        // Deleted alarm IDs may still identify completed grades and ring intents.
                        db.execSQL("DELETE FROM sqlite_sequence WHERE name='alarms'");
                        db.execSQL("INSERT INTO sqlite_sequence(name,seq) VALUES('alarms',?)",
                                new Object[]{DatabaseUtils.longForQuery(old,
                                    "SELECT COALESCE(MAX(seq),0) FROM sqlite_sequence WHERE name='alarms'", null)});
                    }
                } else {
                    importLegacy(db);
                }
                db.execSQL("INSERT INTO import_state(source) VALUES ('credential')");
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        migratedWhileUnlocked = true;
    }

    private static void copyRows(SQLiteDatabase old, SQLiteDatabase db, String table) {
        try (Cursor cursor = old.query(table, null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                ContentValues row = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, row);
                db.insertOrThrow(table, null, row);
            }
        }
    }

    private void importLegacy(SQLiteDatabase db) {
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
        File file = new File(credentialContext.getFilesDir(), name);
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
