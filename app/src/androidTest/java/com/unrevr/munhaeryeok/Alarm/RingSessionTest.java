package com.unrevr.munhaeryeok.Alarm;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RingSessionTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Before @After
    public void clearSession() {
        RingSession.state(context).edit().clear().commit();
    }

    @Test
    public void processDeathAfterGradingSkipsThePersistedHead() {
        RingSession.enqueue(context, new Intent().putExtra("id", 10).putExtra("alarm_id", 5));
        RingSession.enqueue(context, new Intent().putExtra("id", 11).putExtra("alarm_id", 2));
        assertEquals("11/2", RingSession.key(RingSession.nextUngraded(context, "10/5"::equals)));
        assertNull(RingSession.nextUngraded(context, "11/2"::equals));
    }

    @Test
    public void simultaneousDeliveriesRemainDistinctAndGradeInArrivalOrder() {
        Intent first = new Intent("com.unrevr.auk.RING").putExtra("id", 10).putExtra("alarm_id", 1);
        Intent second = new Intent("com.unrevr.auk.RING").putExtra("id", 11).putExtra("alarm_id", 1)
                .putExtra("name", "두 번째 | 알람");
        RingSession.enqueue(context, first);
        RingSession.state(context).edit().putInt("attempts", 1).apply();
        RingSession.enqueue(context, second);
        RingSession.enqueue(context, first);
        assertEquals("10/1", RingSession.key(RingSession.current(context)));
        assertEquals(1, RingSession.state(context).getInt("attempts", 0));
        RingSession.end(context, "10/1");
        RingSession.end(context, "10/1");
        assertEquals("11/1", RingSession.key(RingSession.current(context)));
        assertEquals("두 번째 | 알람", RingSession.current(context).getStringExtra("name"));
        assertFalse(RingSession.state(context).contains("attempts"));
        RingSession.end(context, "11/1");
        assertNull(RingSession.current(context));
    }
}
