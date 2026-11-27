package com.unrevr.munhaeryeok;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.UserManager;

/** Keeps only outstanding pre-unlock answers in device storage. Learning history remains credential protected. */
public final class AlarmGrades extends SQLiteOpenHelper {
    private static final String DATABASE = "pending-alarm-grades.db";
    private static AlarmGrades journal;

    private AlarmGrades(Context context) {
        super(context.createDeviceProtectedStorageContext(), DATABASE, null, 1);
    }

    private static boolean hasJournal(Context context) {
        return context.createDeviceProtectedStorageContext().getDatabasePath(DATABASE).isFile();
    }

    private static synchronized SQLiteDatabase pending(Context context) {
        if (journal == null) journal = new AlarmGrades(context.getApplicationContext());
        return journal.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pending (occurrence TEXT PRIMARY KEY, type INTEGER NOT NULL,"
                + " problem INTEGER NOT NULL, correct INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Unknown pending answer database version " + oldVersion);
    }

    static boolean unlocked(Context context) {
        return context.getSystemService(UserManager.class).isUserUnlocked();
    }

    public static boolean isGraded(Context context, String occurrence) {
        if (unlocked(context)) return UserInfo.getInstance(context).isGraded(occurrence);
        if (!hasJournal(context)) return false;
        try (Cursor rows = pending(context).rawQuery("SELECT 1 FROM pending WHERE occurrence=?",
                new String[]{occurrence})) {
            return rows.moveToFirst();
        }
    }

    public static void record(Context context, String occurrence, Problem problem, boolean correct) {
        if (unlocked(context)) {
            UserInfo.getInstance(context).recordAnswer(occurrence, problem, correct);
            return;
        }
        SQLiteDatabase db = pending(context);
        // execSQL propagates storage errors. Only a duplicate occurrence is ignored.
        db.execSQL("INSERT OR IGNORE INTO pending(occurrence,type,problem,correct) VALUES(?,?,?,?)",
                new Object[]{occurrence, problem.problemType, problem.id, correct ? 1 : 0});
        // Unlock can race the insertion. Opening the profile drains the journal.
        if (unlocked(context)) UserInfo.getInstance(context);
    }

    static void replay(Context context, UserInfo profile) {
        if (!hasJournal(context)) return;
        SQLiteDatabase db = pending(context);
        try (Cursor any = db.rawQuery("SELECT 1 FROM pending LIMIT 1", null)) {
            if (!any.moveToFirst()) return;
        }
        db.beginTransaction();
        try (Cursor rows = db.rawQuery("SELECT occurrence,type,problem,correct FROM pending ORDER BY rowid", null)) {
            while (rows.moveToNext()) {
                profile.recordAnswer(rows.getString(0), rows.getInt(1), rows.getInt(2), rows.getInt(3) != 0);
            }
            db.delete("pending", null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
