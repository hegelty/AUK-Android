package com.unrevr.munhaeryeok;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

final class LearningDatabase extends SQLiteOpenHelper {
    private static LearningDatabase instance;
    private final File legacyDirectory;

    static synchronized LearningDatabase get(Context context) {
        if (instance == null) {
            Context app = context.getApplicationContext();
            instance = new LearningDatabase(app, "learning.db", app.getFilesDir());
        }
        return instance;
    }

    LearningDatabase(Context context, String name, File legacyDirectory) {
        super(context, name, null, 1);
        this.legacyDirectory = legacyDirectory;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQLiteOpenHelper wraps creation and the complete legacy import in one transaction.
        db.execSQL("CREATE TABLE profile (singleton INTEGER PRIMARY KEY CHECK(singleton = 1), "
                + "installation TEXT NOT NULL, solved INTEGER NOT NULL, wrong INTEGER NOT NULL, "
                + "score INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE mistakes (type INTEGER NOT NULL, problem INTEGER NOT NULL, "
                + "UNIQUE(type, problem))");
        db.execSQL("CREATE TABLE graded (occurrence TEXT PRIMARY KEY NOT NULL)");

        String id = legacy("id");
        if (id.isEmpty()) id = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        long solved = legacyCount("solved_count");
        long wrong = legacyCount("wrong_count");
        String oldScore = legacy("score");
        long score = oldScore.isEmpty() ? UserInfo.scoreAfter(solved, wrong) : nonnegative(oldScore);
        db.execSQL("INSERT INTO profile VALUES (1, ?, ?, ?, ?)", new Object[]{id, solved, wrong, score});
        importMistakes(db, Problem.MC, legacy("wrong_mc_problems"));
        importMistakes(db, Problem.SF, legacy("wrong_sf_problems"));
    }

    private String legacy(String field) {
        File file = new File(legacyDirectory, "user_info_" + field);
        if (!file.exists()) return "";
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot import learning field " + field, e);
        }
    }

    private long legacyCount(String field) {
        return nonnegative(legacy(field));
    }

    private static long nonnegative(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void importMistakes(SQLiteDatabase db, int type, String data) {
        for (String token : data.split(",")) {
            try {
                int id = Integer.parseInt(token.trim());
                if (id >= 0) addMistake(db, type, id);
            } catch (NumberFormatException ignored) {
                // Keep valid IDs surrounding a damaged entry.
            }
        }
    }

    static void addMistake(SQLiteDatabase db, int type, int problem) {
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("problem", problem);
        db.insertWithOnConflict("mistakes", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Unknown learning database version " + oldVersion);
    }
}
