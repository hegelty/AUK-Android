package com.unrevr.munhaeryeok.Alarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;

public class AlarmSFActivity extends AppCompatActivity {
    int id, h, m;
    boolean sound, vibration;
    Problem problem;
    int cnt;
    boolean solved;
    Vibrator vibrator;
    SoundPool soundPool;
    int soundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_sf_layout);
        Intent intent = getIntent();
        solved = false;

        id = intent.getIntExtra("id", 0);
        h = intent.getIntExtra("h", 0);
        m = intent.getIntExtra("m", 0);
        sound = intent.getBooleanExtra("sound", false);
        vibration = intent.getBooleanExtra("vibration", false);

        int problem_id = intent.getIntExtra("id", 0);
        if(problem_id!=0) problem = new Problem(Problem.SF, problem_id);
        else problem = new Problem(Problem.SF);

        cnt = 0;

        // vibrate until destroyed
        if (vibration) {
            Log.d("vibration", "vibration");
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 1000, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }

        // play alarm sound until destroyed
        if (sound) {
            Log.d("sound", "sound");
            soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()).build();
            soundID = soundPool.load(getApplicationContext(), R.raw.alarm_sound, 1);
            soundPool.setOnLoadCompleteListener((soundPool1, i, i1) -> soundPool1.play(soundID, 1f, 1f, 0, -1, 1f));
        }

        initLayout();
    }

    void initLayout() {
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView AMPMTextView = findViewById(R.id.AMPMTextView);
        TextView questionTextView = findViewById(R.id.questionTextView);
        TextView hintTextView = findViewById(R.id.hintTextView);
        EditText answerEditText = findViewById(R.id.answerEditText);
        Button closeButton = findViewById(R.id.closeButton);
        Button submitButton = findViewById(R.id.submitButton);

        closeButton.setVisibility(Button.GONE);

        if (h < 12) {
            AMPMTextView.setText("AM");
            timeTextView.setText(String.format("%d:%02d", h, m));
        } else {
            AMPMTextView.setText("PM");
            if(h==12) {
                timeTextView.setText("12:" + String.format("%02d",m));
            } else timeTextView.setText(String.format("%d:%02d", h - 12, m));
        }

        questionTextView.setText(problem.question);

        submitButton.setOnClickListener(v -> {
            if (problem.checkAnswer(answerEditText.getText().toString())) {
                hintTextView.setText(problem.solution);
                answerEditText.setBackgroundResource(R.drawable.round_corner_blue);
                answerEditText.setTextColor(Color.WHITE);
                closeButton.setVisibility(Button.VISIBLE);
                submitButton.setOnClickListener(null);
                addCorrectProblem(problem);
            } else {
                if(++cnt == 2) {
                    hintTextView.setText(problem.solution);
                    answerEditText.setBackgroundResource(R.drawable.round_corner_red);
                    answerEditText.setTextColor(Color.WHITE);
                    answerEditText.setText(problem.sfAnswer);
                    addWrongProblem(problem);
                    closeButton.setVisibility(Button.VISIBLE);
                    submitButton.setOnClickListener(null);
                }
                else {
                    hintTextView.setText(problem.hint);
                    answerEditText.setBackgroundResource(R.drawable.round_corner_red);
                    answerEditText.setTextColor(Color.WHITE);
                    answerEditText.setText("");
                }
            }
        });

        closeButton.setOnClickListener(v -> {
            finish();
        });
    }

    // EditText 포커스 해제
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) {
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
                EditText editText = findViewById(R.id.answerEditText);
                editText.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    void addWrongProblem(Problem problem) {
        solved = true;
        if(sound) soundPool.stop(soundID);
        if(vibration) vibrator.cancel();
    }

    void addCorrectProblem(Problem problem) {
        solved = true;
        if(sound) soundPool.stop(soundID);
        if(vibration) vibrator.cancel();
    }

    // 강제로 끌때 동작
    @Override
    public void onPause() {
        super.onPause();
        if(sound) soundPool.stop(soundID);
        if(vibration) vibrator.cancel();
        if(!solved) {
            Intent intent = new Intent(this, AlarmMCActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("h", h);
            intent.putExtra("m", m);
            intent.putExtra("sound", sound);
            intent.putExtra("vibration", vibration);
            intent.putExtra("problem_id", problem.id);
            startActivity(intent);

            // finish this activity
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sound) soundPool.stop(soundID);
        if(vibration) vibrator.cancel();
        if(!solved) {
            Intent intent = new Intent(this, AlarmMCActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("h", h);
            intent.putExtra("m", m);
            intent.putExtra("sound", sound);
            intent.putExtra("vibration", vibration);
            intent.putExtra("problem_id", problem.id);
            startActivity(intent);

            // finish this activity
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }
}