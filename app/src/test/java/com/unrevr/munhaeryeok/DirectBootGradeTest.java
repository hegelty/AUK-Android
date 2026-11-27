package com.unrevr.munhaeryeok;

import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.database.DatabaseUtils;
import android.os.UserManager;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 36)
public class DirectBootGradeTest {
    @Test
    public void gradeBeforeFirstUnlockIsReplayedExactlyOnce() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UserManager user = context.getSystemService(UserManager.class);
        shadowOf(user).setUserUnlocked(false);
        String occurrence = "boot-test/42";
        Problem problem = Problem.byId(context, Problem.MC, 0);
        assertFalse(AlarmGrades.isGraded(context, occurrence));
        AlarmGrades.record(context, occurrence, problem, true);
        AlarmGrades.record(context, occurrence, problem, false);
        assertTrue(AlarmGrades.isGraded(context, occurrence));
        assertFalse(context.getDatabasePath("learning.db").exists());
        assertTrue(context.createDeviceProtectedStorageContext().getDatabasePath("pending-alarm-grades.db").isFile());
        shadowOf(user).setUserUnlocked(true);
        UserInfo first = UserInfo.getInstance(context);
        first.getUserInfo();
        assertTrue(first.isGraded(occurrence));
        assertEquals(50, first.score);
        UserInfo again = UserInfo.getInstance(context);
        again.getUserInfo();
        assertTrue(again.getWrongProblems(Problem.MC).isEmpty());
        assertEquals(50, again.score);
        assertEquals(1, DatabaseUtils.longForQuery(LearningDatabase.get(context).getReadableDatabase(),
                "SELECT solved FROM profile WHERE singleton=1", null));
        assertEquals(0, DatabaseUtils.longForQuery(LearningDatabase.get(context).getReadableDatabase(),
                "SELECT wrong FROM profile WHERE singleton=1", null));
        shadowOf(user).setUserUnlocked(false);
        assertFalse(AlarmGrades.isGraded(context, occurrence));
        shadowOf(user).setUserUnlocked(true);
    }
}
