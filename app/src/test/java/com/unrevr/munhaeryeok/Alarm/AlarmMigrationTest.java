package com.unrevr.munhaeryeok.Alarm;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.Collections;
import org.junit.Test;

public class AlarmMigrationTest {
    @Test public void importsGroupDaysNamesFavoritesAndLegacyRequestCodes() {
        String groups = "3|8:5|true|false|A-B=C|D|3|true|71,0,0,76,0,0,0,="
                + "4|9:0|false|true|저녁|2|false|0,77,0,0,0,0,0,=";
        String entries = "71-123-1:8:5:0-A-B=C|D|1|0|3=76-0-4:8:5:0-A-B=C|D|1|0|3=";
        LegacyAlarms legacy = new LegacyAlarms(groups, entries);
        assertEquals(Collections.singleton(123), legacy.requestCodes);
        assertEquals(2, legacy.groups.size());
        AlarmData first = legacy.groups.get(0);
        assertEquals("A-B=C|D", first.name);
        assertEquals("일, 수", first.dayLabel());
        assertTrue(first.favorite);
        assertEquals(3, first.id);
        assertEquals(4, legacy.groups.get(1).id);
    }

    @Test public void recoversOccurrenceWhenLegacyGroupWriteWasInterrupted() {
        LegacyAlarms legacy = new LegacyAlarms("", "100-7-2:23:59:0-야간|0|1|2=");
        assertEquals(1, legacy.groups.size());
        AlarmData alarm = legacy.groups.get(0);
        assertTrue(alarm.vibration);
        assertFalse(alarm.sound);
        assertEquals("월", alarm.dayLabel());
    }

    @Test public void advancesAcrossWeekAtScheduledMinute() {
        AlarmData sunday = new AlarmData(1, 7, 0, 1, true, false, "알람", 1, false);
        ZonedDateTime now = ZonedDateTime.parse("2026-11-29T07:00:00+09:00[Asia/Seoul]");
        assertEquals(now.toInstant().toEpochMilli(), sunday.nextAfter(now.minusNanos(1)));
        assertEquals(now.plusWeeks(1).toInstant().toEpochMilli(), sunday.nextAfter(now));
    }
}
