package com.unrevr.munhaeryeok.Alarm;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

public final class AlarmController {
    static final String RING_CHANNEL = "ringing_alarms_v2";
    private static final String ACTION_RING = "com.unrevr.auk.RING";
    private static final String[] COLUMNS = {"id", "hour", "minute", "days", "sound", "vibration",
            "name", "problem", "favorite", "due", "token"};
    private final Context context;
    private final SQLiteDatabase db;
    private final AlarmManager manager;

    public AlarmController(Context context) {
        this.context = context.getApplicationContext();
        db = AlarmDatabase.get(this.context).getWritableDatabase();
        manager = context.getSystemService(AlarmManager.class);
    }

    boolean exactAllowed() {
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms();
    }

    boolean notificationsAllowed() {
        NotificationManager notifications = context.getSystemService(NotificationManager.class);
        NotificationChannel channel = notifications.getNotificationChannel(RING_CHANNEL);
        return notifications.areNotificationsEnabled()
                && (channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
    }

    public boolean canRing() {
        return exactAllowed() && notificationsAllowed();
    }

    private static AlarmData read(Cursor c) {
        return new AlarmData(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3),
                c.getInt(4) != 0, c.getInt(5) != 0, c.getString(6), c.getInt(7), c.getInt(8) != 0);
    }

    private Cursor byId(int id) {
        return db.query("alarms", COLUMNS, "id=?", new String[]{Integer.toString(id)}, null, null, null);
    }

    AlarmData get(int id) {
        try (Cursor c = byId(id)) {
            return c.moveToFirst() ? read(c) : null;
        }
    }

    ArrayList<AlarmData> list(boolean favoritesOnly) {
        ArrayList<AlarmData> rows = new ArrayList<>();
        try (Cursor c = db.query("alarms", COLUMNS, favoritesOnly ? "favorite=1" : null, null,
                null, null, "id")) {
            while (c.moveToNext()) rows.add(read(c));
        }
        return rows;
    }

    public AlarmData nearest() {
        if (!canRing()) return null;
        try (Cursor c = db.query("alarms", COLUMNS, "due>0", null, null, null, "due,id", "1")) {
            return c.moveToFirst() ? read(c) : null;
        }
    }

    public boolean hasAlarms() {
        try (Cursor c = db.rawQuery("SELECT 1 FROM alarms LIMIT 1", null)) {
            return c.moveToFirst();
        }
    }

    int save(AlarmData alarm) {
        int id = alarm.id;
        db.beginTransaction();
        try {
            ContentValues values = AlarmDatabase.values(alarm);
            if (id == 0) {
                id = Math.toIntExact(db.insertOrThrow("alarms", null, values));
            } else {
                // Invalidate a delivery already queued for the previous configuration.
                values.put("due", 0);
                if (db.update("alarms", values, "id=?", new String[]{Integer.toString(id)}) == 0)
                    throw new IllegalStateException("삭제된 알람입니다.");
            }
            try (Cursor row = byId(id)) {
                if (row.moveToFirst()) schedule(row, System.currentTimeMillis(), canRing(), false, true);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return id;
    }

    void delete(int id) {
        db.delete("alarms", "id=?", new String[]{Integer.toString(id)});
        cancel(new Intent(context, AlarmReceiver.class).setAction(ACTION_RING), id);
    }

    void setFavorite(int id, boolean favorite) {
        ContentValues values = new ContentValues();
        values.put("favorite", favorite);
        db.update("alarms", values, "id=?", new String[]{Integer.toString(id)});
    }

    private void cancel(Intent intent, int code) {
        PendingIntent previous = PendingIntent.getBroadcast(context, code, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (previous != null) {
            manager.cancel(previous);
            previous.cancel();
        }
    }

    /** Rebuild platform alarms from the durable schedule (including after reboot or a permission change). */
    public void reloadAlarms() {
        reconcile(true);
    }

    public void refreshAlarms() {
        reconcile(false);
    }

    private void reconcile(boolean force) {
        db.beginTransaction();
        try {
            try (Cursor codes = db.rawQuery("SELECT code FROM legacy_requests", null)) {
                while (codes.moveToNext()) cancel(new Intent(context, AlarmReceiver.class), codes.getInt(0));
            }
            db.delete("legacy_requests", null, null);
            // Snapshot IDs before updating so large databases aren't re-queried mid-iteration.
            ArrayList<Integer> ids = new ArrayList<>();
            try (Cursor rows = db.rawQuery("SELECT id FROM alarms", null)) {
                while (rows.moveToNext()) ids.add(rows.getInt(0));
            }
            long now = System.currentTimeMillis();
            boolean allowed = canRing();
            for (int id : ids) {
                try (Cursor c = byId(id)) {
                    if (c.moveToFirst()) schedule(c, now, allowed, true, force);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Claim a specific delivery once and persist the following occurrence before presenting it. */
    public boolean consumeOccurrence(int id, int occurrenceId) {
        db.beginTransaction();
        try (Cursor c = byId(id)) {
            if (!c.moveToFirst() || occurrenceId == 0 || occurrenceId != c.getInt(10) || c.getLong(9) == 0)
                return false;
            boolean allowed = canRing();
            schedule(c, Math.max(System.currentTimeMillis(), c.getLong(9)), allowed, false, true);
            db.setTransactionSuccessful();
            return allowed;
        } finally {
            db.endTransaction();
        }
    }

    private void schedule(Cursor row, long after, boolean allowed, boolean restoring, boolean force) {
        AlarmData alarm = read(row);
        Intent ring = new Intent(context, AlarmReceiver.class).setAction(ACTION_RING);
        int token = row.getInt(10);
        long due = 0;
        if (allowed) {
            due = alarm.nextAfter(Instant.ofEpochMilli(after).atZone(ZoneId.systemDefault()));
            // A foreground refresh can race delivery in the same second. Keep its token
            // and let the receiver claim the due occurrence exactly once.
            long previousDue = row.getLong(9);
            if (restoring && previousDue <= after && previousDue > after - 60_000) due = previousDue;
            // A restored database can contain future due times without any platform intents.
            if (!force && due == previousDue && PendingIntent.getBroadcast(context, alarm.id, ring,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null) return;
            if (due != previousDue) token = token == Integer.MAX_VALUE ? 1 : token + 1;
            ring.putExtra("id", alarm.id).putExtra("alarm_id", token)
                    .putExtra("h", alarm.h).putExtra("m", alarm.m).putExtra("name", alarm.name)
                    .putExtra("sound", alarm.sound).putExtra("vibration", alarm.vibration)
                    .putExtra("problem_type", alarm.problemType);
            PendingIntent delivery = PendingIntent.getBroadcast(context, alarm.id, ring,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Intent edit = new Intent(context, AlarmSettingActivity.class).putExtra("id", alarm.id);
            PendingIntent show = PendingIntent.getActivity(context, alarm.id, edit,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            try {
                manager.setAlarmClock(new AlarmManager.AlarmClockInfo(due, show), delivery);
            } catch (SecurityException revokedWhileScheduling) {
                due = 0;
            }
        }
        if (due == 0 && !force && row.getLong(9) == 0) return;
        if (due == 0) cancel(ring, alarm.id);
        db.execSQL("UPDATE alarms SET due=?, token=? WHERE id=?", new Object[]{due, token, alarm.id});
    }
}
