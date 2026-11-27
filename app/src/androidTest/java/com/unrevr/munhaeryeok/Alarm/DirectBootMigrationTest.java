package com.unrevr.munhaeryeok.Alarm;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DirectBootMigrationTest {
    @Test public void importsCredentialScheduleOnceAfterDeviceDatabaseWasOpenedEarly() {
        Context cp = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context dp = cp.createDeviceProtectedStorageContext();
        String name = "direct-boot-test-" + UUID.randomUUID() + ".db";
        try {
            try (AlarmDatabase target = new AlarmDatabase(cp, name)) {
                // A locked boot can already have created an empty device database.
                SQLiteDatabase device = target.getWritableDatabase();
                assertTrue(dp.getDatabasePath(name).exists());
                try (SQLiteDatabase old = cp.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)) {
                    old.execSQL("CREATE TABLE alarms(id INTEGER PRIMARY KEY AUTOINCREMENT, hour, minute,"
                            + " days, sound, vibration, name, problem, favorite, due, token)");
                    old.execSQL("CREATE TABLE legacy_requests(code INTEGER PRIMARY KEY)");
                    old.execSQL("INSERT INTO alarms VALUES(3,8,5,9,1,1,'기상',3,1,111,7)");
                    old.execSQL("INSERT INTO legacy_requests VALUES(4321)");
                    old.execSQL("INSERT INTO alarms(id) VALUES(27)");
                    old.execSQL("DELETE FROM alarms WHERE id=27");
                }
                target.importAfterUnlock();
                assertEquals(7, DatabaseUtils.longForQuery(device, "SELECT token FROM alarms WHERE id=3", null));
                assertEquals(4321, DatabaseUtils.longForQuery(device, "SELECT code FROM legacy_requests", null));
                assertEquals(27, DatabaseUtils.longForQuery(device, "SELECT seq FROM sqlite_sequence WHERE name='alarms'", null));
                device.delete("alarms", "id=3", null);
            }
            try (AlarmDatabase reopened = new AlarmDatabase(cp, name)) {
                reopened.importAfterUnlock();
                assertEquals(0, DatabaseUtils.queryNumEntries(reopened.getReadableDatabase(), "alarms"));
            }
        } finally {
            cp.deleteDatabase(name);
            dp.deleteDatabase(name);
        }
    }
}
