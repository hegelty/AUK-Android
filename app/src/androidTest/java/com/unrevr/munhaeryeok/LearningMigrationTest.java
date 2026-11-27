package com.unrevr.munhaeryeok;

import android.content.Context;
import android.database.Cursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LearningMigrationTest {
    @Test public void coldStartImportsExistingFilesAndGradesEachOccurrenceOnce() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File legacy = Files.createTempDirectory(context.getCacheDir().toPath(), "learning-test").toFile();
        String dbName = "learning-test-" + UUID.randomUUID() + ".db";
        try {
            writeLegacy(legacy, "id", "existing-installation");
            writeLegacy(legacy, "solved_count", "3");
            writeLegacy(legacy, "wrong_count", "1");
            writeLegacy(legacy, "score", "37");
            writeLegacy(legacy, "wrong_mc_problems", "0,broken,1,0,");
            Problem first = Problem.byId(context, Problem.MC, 0);
            assertEquals(0, first.id);
            assertSame(first, Problem.byId(context, Problem.MC, 0));
            try (LearningDatabase firstOpen = new LearningDatabase(context, dbName, legacy)) {
                UserInfo user = new UserInfo(firstOpen);
                assertEquals(Arrays.asList(0, 1), user.getWrongProblems(Problem.MC));
                // The alarm can submit its answer before the home screen ever loads a profile.
                user.recordAnswer("alarm:1", Problem.byId(context, Problem.SF, 0), false);
            }
            writeLegacy(legacy, "id", "stale-after-migration");
            try (LearningDatabase reopened = new LearningDatabase(context, dbName, legacy)) {
                UserInfo user = new UserInfo(reopened);
                user.recordAnswer("alarm:1", first, true);
                assertTrue(user.isGraded("alarm:1"));
                assertFalse(user.isGraded("alarm:2"));
                user.recordAnswer("alarm:2", first, true);
                user.getUserInfo();
                assertEquals("existing-installation", user.id);
                assertEquals(34, user.score);
                assertEquals(Arrays.asList(0), user.getWrongProblems(Problem.SF));
                try (Cursor row = reopened.getReadableDatabase().rawQuery(
                        "SELECT solved, wrong FROM profile", null)) {
                    assertTrue(row.moveToFirst());
                    assertEquals(4, row.getInt(0));
                    assertEquals(2, row.getInt(1));
                }
            }
        } finally {
            context.deleteDatabase(dbName);
            File[] files = legacy.listFiles();
            if (files != null) for (File file : files) Files.delete(file.toPath());
            Files.delete(legacy.toPath());
        }
    }

    private static void writeLegacy(File directory, String name, String contents) throws Exception {
        Files.write(new File(directory, "user_info_" + name).toPath(),
                contents.getBytes(StandardCharsets.UTF_8));
    }
}
