package com.unrevr.munhaeryeok.Alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 36)
public class AlarmDeliveryTest {
    @Test
    public void importedAlarmCanRecoverPermissionRescheduleAndRejectReplays() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserManager user = context.getSystemService(UserManager.class);
        shadowOf(user).setUserUnlocked(true);
        ShadowAlarmManager platform = shadowOf(context.getSystemService(AlarmManager.class));
        shadowOf(context.getSystemService(NotificationManager.class)).setNotificationsEnabled(true);
        ShadowAlarmManager.setCanScheduleExactAlarms(false);
        String label = "평일 | 일과-알림=테스트";
        Path files = context.getFilesDir().toPath();
        String legacyGroup = "3|8:45|true|false|" + label + "|1|true|0,77,0,0,0,0,0,=";
        Files.write(files.resolve("alarm_data_alarms_list"), legacyGroup.getBytes(StandardCharsets.UTF_8));
        String legacyEntry = "77-123-2:8:45:0-" + label + "|1|0|1=";
        Files.write(files.resolve("alarm_alarm_list"), legacyEntry.getBytes(StandardCharsets.UTF_8));
        AlarmController alarms = new AlarmController(context);
        int id = 3;
        AlarmData imported = alarms.get(id);
        assertEquals(label, imported.name);
        assertEquals("월", imported.dayLabel());
        assertTrue(imported.favorite);
        alarms.refreshAlarms();
        assertTrue(alarms.hasAlarms());
        assertNull(alarms.nearest());
        assertNull(platform.peekNextScheduledAlarm());

        ShadowAlarmManager.setCanScheduleExactAlarms(true);
        new AlarmRecoveryReceiver().onReceive(context,
                new Intent(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED));
        assertEquals(label, alarms.nearest().name);
        assertEquals(1, platform.getScheduledAlarms().size());

        PendingIntent first = platform.peekNextScheduledAlarm().operation;
        Intent stale = new Intent(shadowOf(first).getSavedIntent());
        alarms.save(new AlarmData(id, 9, 15, 127, true, false, label, 1, true));
        int staleToken = stale.getIntExtra("alarm_id", 0);
        assertNotEquals(0, staleToken);
        assertFalse(alarms.consumeOccurrence(id, staleToken));
        Intent valid = new Intent(shadowOf(platform.peekNextScheduledAlarm().operation).getSavedIntent());
        assertEquals(1, platform.getScheduledAlarms().size());
        long due = platform.peekNextScheduledAlarm().getTriggerAtMs();
        new AlarmReceiver().onReceive(context, valid);
        assertTrue(platform.peekNextScheduledAlarm().getTriggerAtMs() > due);
        assertFalse(alarms.consumeOccurrence(id, valid.getIntExtra("alarm_id", 0)));
        assertEquals(RingSession.key(valid), RingSession.key(RingSession.current(context)));
        assertEquals(1, platform.getScheduledAlarms().size());

        shadowOf(user).setUserUnlocked(false);
        new AlarmRecoveryReceiver().onReceive(context, new Intent(Intent.ACTION_LOCKED_BOOT_COMPLETED));
        assertNull(RingSession.current(context));
        Intent afterBoot = new Intent(shadowOf(platform.peekNextScheduledAlarm().operation).getSavedIntent());
        new AlarmReceiver().onReceive(context, afterBoot);
        String ringing = RingSession.key(afterBoot);
        assertTrue(RingSession.isActive(context, ringing));
        shadowOf(user).setUserUnlocked(true);
        new AlarmRecoveryReceiver().onReceive(context, new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertTrue(RingSession.isActive(context, ringing));
        assertEquals(1, platform.getScheduledAlarms().size());
        alarms.delete(id);
        assertFalse(alarms.hasAlarms());
        assertNull(platform.peekNextScheduledAlarm());
    }
}
