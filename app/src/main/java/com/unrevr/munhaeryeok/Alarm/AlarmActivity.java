package com.unrevr.munhaeryeok.Alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.unrevr.munhaeryeok.InsetActivity;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.AlarmGrades;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class AlarmActivity extends InsetActivity {
    private static final int[] CHOICES = {R.id.answer1, R.id.answer2, R.id.answer3, R.id.answer4};
    private static final int[] LABELS = {R.id.answerTextView1, R.id.answerTextView2,
            R.id.answerTextView3, R.id.answerTextView4};
    private static final int[] MARKS = {R.id.oxTextView1, R.id.oxTextView2, R.id.oxTextView3, R.id.oxTextView4};
    private String occurrence;
    private Problem problem;
    private int attempts, wrongChoices;
    private boolean solved, correct;
    private CountDownTimer timer;
    private SharedPreferences session;
    private final SharedPreferences.OnSharedPreferenceChangeListener sessionChanged = (prefs, key) -> {
        if (!solved && !RingSession.isActive(this, occurrence)) finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        session = RingSession.state(this);
        showOccurrence(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (occurrence != null && occurrence.equals(RingSession.key(intent))) return;
        setIntent(intent);
        showOccurrence(null);
    }

    private void showOccurrence(Bundle saved) {
        if (timer != null) timer.cancel();
        Intent intent = getIntent();
        occurrence = RingSession.key(intent);
        solved = saved != null && saved.getBoolean("solved");
        if (!solved && RingSession.isActive(this, occurrence)
                && AlarmGrades.isGraded(this, occurrence)) {
            RingSession.end(this, occurrence);
            startService(new Intent(this, AlarmService.class));
        }
        if (!solved && !RingSession.isActive(this, occurrence)) {
            finish();
            return;
        }
        if (solved) {
            problem = Problem.byId(this, saved.getInt("type"), saved.getInt("problem"));
            correct = saved.getBoolean("correct");
            wrongChoices = saved.getInt("wrong_choices");
        } else {
            correct = false;
            int type = session.getInt("type", intent.getIntExtra("problem_type", Problem.MC));
            if (type != Problem.MC && type != Problem.SF) {
                type = ThreadLocalRandom.current().nextBoolean() ? Problem.MC : Problem.SF;
            }
            int question = session.getInt("problem", -1);
            if (Problem.hasId(this, type, question)) {
                problem = Problem.byId(this, type, question);
            } else {
                problem = Problem.random(this, type);
                session.edit().putInt("type", type).putInt("problem", problem.id).apply();
            }
            attempts = session.getInt("attempts", 0);
            wrongChoices = session.getInt("wrong_choices", 0);
        }

        setContentView(problem.problemType == Problem.MC ? R.layout.alarm_layout_mc : R.layout.alarm_layout_sf);
        int hour = intent.getIntExtra("h", 0);
        int minute = intent.getIntExtra("m", 0);
        label(R.id.problemTextView).setText(String.format(Locale.getDefault(), "%d:%02d", (hour + 11) % 12 + 1, minute));
        label(R.id.AMPMTextView).setText(hour < 12 ? "AM" : "PM");
        label(R.id.questionTextView).setText(problem.question);
        findViewById(R.id.closeButton).setOnClickListener(v -> finish());
        if (problem.problemType == Problem.MC) {
            for (int i = 0; i < CHOICES.length; i++) {
                final int choice = i;
                label(LABELS[i]).setText(problem.mcAnswers[i]);
                findViewById(CHOICES[i]).setOnClickListener(v -> submit(problem.checkAnswer(choice), choice));
            }
        } else {
            EditText answer = findViewById(R.id.answerEditText);
            findViewById(R.id.submitButton).setOnClickListener(v -> submit(problem.checkAnswer(answer.getText().toString()), -1));
        }
        renderFeedback();
        if (!solved) showCooldown();
    }

    private TextView label(int id) {
        return findViewById(id);
    }

    private void submit(boolean matches, int choice) {
        if (solved || !RingSession.isActive(this, occurrence)
                || SystemClock.elapsedRealtime() < session.getLong("cooldown_until", 0)) return;
        if (!matches) {
            attempts++;
            if (choice >= 0) wrongChoices |= 1 << choice;
        }
        if (matches || attempts >= 2) {
            AlarmGrades.record(this, occurrence, problem, matches);
            correct = matches;
            solved = true;
            RingSession.end(this, occurrence);
            startService(new Intent(this, AlarmService.class));
        } else {
            session.edit().putInt("attempts", attempts).putInt("wrong_choices", wrongChoices)
                    .putLong("cooldown_until", SystemClock.elapsedRealtime() + 5000).apply();
            if (problem.problemType == Problem.SF) label(R.id.answerEditText).setText("");
            showCooldown();
        }
        renderFeedback();
    }

    private void renderFeedback() {
        findViewById(R.id.closeButton).setVisibility(solved ? View.VISIBLE : View.GONE);
        if (!solved && attempts == 0) return;
        label(R.id.hintTextView).setText(solved ? problem.solution : problem.hint);
        if (problem.problemType == Problem.SF) {
            EditText input = findViewById(R.id.answerEditText);
            input.setBackgroundResource(correct ? R.drawable.round_corner_blue : R.drawable.round_corner_red);
            input.setTextColor(Color.WHITE);
            if (solved) {
                input.setText(problem.sfAnswer);
                input.setEnabled(false);
                findViewById(R.id.submitButton).setEnabled(false);
            }
        } else {
            for (int i = 0; i < CHOICES.length; i++) {
                boolean right = solved && problem.checkAnswer(i);
                if (right || (wrongChoices & (1 << i)) != 0) {
                    findViewById(CHOICES[i]).setBackgroundResource(right ? R.drawable.round_corner_blue : R.drawable.round_corner_red);
                    label(LABELS[i]).setTextColor(Color.WHITE);
                    label(MARKS[i]).setText(right ? "O" : "X");
                }
            }
        }
    }

    private void showCooldown() {
        long remaining = session.getLong("cooldown_until", 0) - SystemClock.elapsedRealtime();
        if (remaining <= 0) return;
        if (timer != null) timer.cancel();
        TextView countdown = label(R.id.coolDownTextView);
        countdown.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown.setText(getString(R.string.ringing_cooldown, (millisUntilFinished + 999) / 1000));
            }

            @Override
            public void onFinish() {
                countdown.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View focused = getCurrentFocus();
        if (event.getAction() == MotionEvent.ACTION_UP && focused instanceof EditText) {
            Rect bounds = new Rect();
            focused.getGlobalVisibleRect(bounds);
            if (!bounds.contains((int) event.getRawX(), (int) event.getRawY())) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(focused.getWindowToken(), 0);
                focused.clearFocus();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        session.registerOnSharedPreferenceChangeListener(sessionChanged);
        if (!solved && !RingSession.isActive(this, occurrence)) finish();
    }

    @Override
    protected void onStop() {
        session.unregisterOnSharedPreferenceChangeListener(sessionChanged);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (problem == null) return;
        out.putBoolean("solved", solved);
        out.putBoolean("correct", correct);
        out.putInt("wrong_choices", wrongChoices);
        out.putInt("problem", problem.id);
        out.putInt("type", problem.problemType);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
