package com.unrevr.munhaeryeok;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public final class UserInfo {
    private final LearningDatabase storage;
    public String id;
    public int score;

    UserInfo(Context context) {
        this(LearningDatabase.get(context));
        AlarmGrades.replay(context, this);
    }

    UserInfo(LearningDatabase storage) {
        this.storage = storage;
    }

    public static UserInfo getInstance(Context context) {
        return new UserInfo(context);
    }

    public void init() {
        getUserInfo();
    }

    public void getUserInfo() {
        try (Cursor row = storage.getReadableDatabase().rawQuery(
                "SELECT installation, score FROM profile WHERE singleton = 1", null)) {
            row.moveToFirst();
            id = row.getString(0);
            score = row.getInt(1);
        }
    }

    public List<Integer> getWrongProblems(int type) {
        List<Integer> problems = new ArrayList<>();
        try (Cursor rows = storage.getReadableDatabase().rawQuery(
                "SELECT problem FROM mistakes WHERE type = ? ORDER BY rowid",
                new String[]{String.valueOf(type)})) {
            while (rows.moveToNext()) problems.add(rows.getInt(0));
        }
        return problems;
    }

    public boolean isGraded(String key) {
        try (Cursor row = storage.getReadableDatabase().rawQuery(
                "SELECT 1 FROM graded WHERE occurrence = ?", new String[]{key})) {
            return row.moveToFirst();
        }
    }

    public void recordAnswer(String occurrenceKey, Problem problem, boolean correct) {
        recordAnswer(occurrenceKey, problem.problemType, problem.id, correct);
    }

    void recordAnswer(String occurrenceKey, int type, int problemId, boolean correct) {
        if (occurrenceKey == null || occurrenceKey.isEmpty()) {
            throw new IllegalArgumentException("Missing alarm occurrence");
        }
        SQLiteDatabase db = storage.getWritableDatabase();
        db.beginTransaction();
        try {
            if (isGraded(occurrenceKey)) return;
            db.execSQL("INSERT INTO graded VALUES (?)", new Object[]{occurrenceKey});
            try (Cursor row = db.rawQuery("SELECT solved, wrong FROM profile WHERE singleton = 1", null)) {
                row.moveToFirst();
                long solved = row.getLong(0) + (correct ? 1 : 0);
                long wrong = row.getLong(1) + (correct ? 0 : 1);
                db.execSQL("UPDATE profile SET solved = ?, wrong = ?, score = ? WHERE singleton = 1",
                        new Object[]{solved, wrong, scoreAfter(solved, wrong)});
            }
            if (!correct) LearningDatabase.addMistake(db, type, problemId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    static int scoreAfter(long solved, long wrong) {
        long total = solved + wrong;
        long accuracy = total == 0 ? 0 : solved * 100 / total;
        return (int) (accuracy / 2 + Math.min(solved, 200) / 4);
    }
}
