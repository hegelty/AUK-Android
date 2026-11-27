package com.unrevr.munhaeryeok;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.TestScheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class HomeAvailabilityTest {
    @Test
    public void homeCanDrawWithoutCompletingNetworkRequests() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean wasSharing = ScoreSharing.isEnabled(context);
        ScoreSharing.setEnabled(context, false);
        TestScheduler stalledNetwork = new TestScheduler();
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> stalledNetwork);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                View root = activity.findViewById(android.R.id.content);
                assertFalse("The first frame was cancelled", root.getViewTreeObserver().dispatchOnPreDraw());
                assertTrue(activity.findViewById(R.id.upcommingAlarm).isEnabled());
                assertTrue(activity.findViewById(R.id.restudyText).isEnabled());
            });
        } finally {
            RxJavaPlugins.setIoSchedulerHandler(null);
            ScoreSharing.setEnabled(context, wasSharing);
        }
    }
}
