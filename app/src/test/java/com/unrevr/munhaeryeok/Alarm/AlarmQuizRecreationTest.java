package com.unrevr.munhaeryeok.Alarm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.UserInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {28, 36})
public class AlarmQuizRecreationTest {
    @Test
    public void rotatesBeforeAndAfterGradingAndRetainsFirstQuestion() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent ring = new Intent(context, AlarmActivity.class).setAction("com.unrevr.auk.RING")
                .putExtra("id", 70).putExtra("alarm_id", 42).putExtra("problem_type", Problem.SF);
        RingSession.enqueue(context, ring);
        RingSession.state(context).edit().putInt("type", Problem.SF).putInt("problem", 0).commit();
        Problem first = Problem.byId(context, Problem.SF, 0);

        try (ActivityScenario<AlarmActivity> activity = ActivityScenario.launch(ring)) {
            activity.recreate();
            activity.onActivity(screen -> {
                TextView question = screen.findViewById(R.id.questionTextView);
                assertEquals(first.question, question.getText().toString());
                EditText answer = screen.findViewById(R.id.answerEditText);
                answer.setText(first.sfAnswer);
                assertTrue(screen.findViewById(R.id.submitButton).performClick());
                assertEquals(View.VISIBLE, screen.findViewById(R.id.closeButton).getVisibility());
            });
            activity.recreate();
            activity.onActivity(screen -> {
                assertEquals(View.VISIBLE, screen.findViewById(R.id.closeButton).getVisibility());
                assertFalse(screen.findViewById(R.id.answerEditText).isEnabled());
            });
        }
        UserInfo stats = UserInfo.getInstance(context);
        stats.getUserInfo();
        assertTrue(stats.isGraded("70/42"));
        assertEquals(50, stats.score);
        assertNull(RingSession.current(context));
    }
}
