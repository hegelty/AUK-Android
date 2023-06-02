package com.unrevr.munhaeryeok.Alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.UserInfo;

import java.util.ArrayList;

public class AlarmActivity extends AppCompatActivity {
    int id, h, m, problem_type;
    boolean sound, vibration;
    Problem problem;
    int cnt = 0;
    boolean solved;
    Vibrator vibrator;
    KeyguardManager keyguardManager;
    Boolean onCoolDown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        Log.d("AlarmSFActivity", "onCreate: SF");

        setTurnScreenOn(true);
        setShowWhenLocked(true);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "app:munhaeryeok");
        wakeLock.acquire(3000);

        solved = false;

        id = intent.getIntExtra("id", 0);
        h = intent.getIntExtra("h", 0);
        m = intent.getIntExtra("m", 0);
        sound = intent.getBooleanExtra("sound", false);
        vibration = intent.getBooleanExtra("vibration", false);
        // 1: 객관, 2: 주관
        problem_type = intent.getIntExtra("problem_type", 1);
        int problem_id = intent.getIntExtra("id", 0);

        if(problem_type == Problem.MC) {
            if(problem_id!=0) problem = new Problem(Problem.MC, problem_id);
            else problem = new Problem(Problem.MC);
            setContentView(R.layout.alarm_layout_mc);
        }
        else {
            if (problem_id != 0) problem = new Problem(Problem.SF, problem_id);
            else problem = new Problem(Problem.SF);
            setContentView(R.layout.alarm_layout_sf);
        }

        Log.d("AlarmActivity", "onCreate: id: " + id + ", h: " + h + ", m: " + m + ", sound: " + sound + ", vibration: " + vibration + (problem_type == 1 ? "MC" : "SF"));
        initLayout();

        if (sound) playSound();
        // vibrate until destroyed
        if (vibration) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 1000, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }

    }

    void initLayout() {
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView AMPMTextView = findViewById(R.id.AMPMTextView);
        TextView questionTextView = findViewById(R.id.questionTextView);
        TextView hintTextView = findViewById(R.id.hintTextView);
        Button closeButton = findViewById(R.id.closeButton);
        Button submitButton = findViewById(R.id.submitButton);

        if (problem_type == Problem.SF) {
            EditText answerEditText = findViewById(R.id.answerEditText);

            submitButton.setOnClickListener(v -> {
                if(onCoolDown) return;
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
        }
        else {
            TextView[] answerTextView = new TextView[4];
            answerTextView[0] = findViewById(R.id.answerTextView1);
            answerTextView[1] = findViewById(R.id.answerTextView2);
            answerTextView[2] = findViewById(R.id.answerTextView3);
            answerTextView[3] = findViewById(R.id.answerTextView4);
            ConstraintLayout[] answerLayout = new ConstraintLayout[4];
            answerLayout[0] = findViewById(R.id.answer1);
            answerLayout[1] = findViewById(R.id.answer2);
            answerLayout[2] = findViewById(R.id.answer3);
            answerLayout[3] = findViewById(R.id.answer4);
            TextView[] OXTextView = new TextView[4];
            OXTextView[0] = findViewById(R.id.oxTextView1);
            OXTextView[1] = findViewById(R.id.oxTextView2);
            OXTextView[2] = findViewById(R.id.oxTextView3);
            OXTextView[3] = findViewById(R.id.oxTextView4);

            for (int i = 0; i < 4; i++) {
                answerTextView[i].setText(problem.mcAnswers[i]);
                int I = i;
                answerLayout[i].setOnClickListener(v -> {
                    if (onCoolDown) return;
                    if (problem.checkAnswer(I)) {
                        answerLayout[I].setBackgroundResource(R.drawable.round_corner_blue);
                        OXTextView[I].setText("O");
                        answerTextView[I].setTextColor(Color.WHITE);
                        hintTextView.setText(problem.solution);
                        closeButton.setVisibility(Button.VISIBLE);
                        for(int j=0;j<4;j++) answerLayout[j].setOnClickListener(null); // 클릭 비활성화
                        addCorrectProblem(problem);
                    } else {
                        if(++cnt==2) {
                            answerLayout[I].setBackgroundResource(R.drawable.round_corner_red);
                            OXTextView[I].setText("X");
                            answerTextView[I].setTextColor(Color.WHITE);
                            closeButton.setVisibility(Button.VISIBLE);
                            hintTextView.setText(problem.solution);
                            for(int j=0;j<4;j++) {
                                if(problem.checkAnswer(j)) {
                                    answerLayout[j].setBackgroundResource(R.drawable.round_corner_blue);
                                    OXTextView[j].setText("O");
                                    answerTextView[j].setTextColor(Color.WHITE);
                                }
                                answerLayout[j].setOnClickListener(null);
                            }
                            addWrongProblem(problem);
                        }
                        else {
                            answerLayout[I].setBackgroundResource(R.drawable.round_corner_red);
                            OXTextView[I].setText("X");
                            answerTextView[I].setTextColor(Color.WHITE);
                            hintTextView.setText(problem.hint);
                            coolDown();
                        }
                    }
                });
            }
        }

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
        closeButton.setVisibility(Button.GONE);
        closeButton.setOnClickListener(v -> {
            finish();
        });
    }

    // EditText 포커스 해제
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(problem_type == Problem.SF) {
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
        }
        return super.dispatchTouchEvent(ev);
    }

    void addWrongProblem(Problem problem) {
        UserInfo.getInstance(getApplicationContext()).wrong(problem.id, problem_type);
        solved = true;
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
    }

    void addCorrectProblem(Problem problem) {
        UserInfo.getInstance(getApplicationContext()).solve();
        solved = true;
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
    }

    // 강제로 끌때 동작
    @Override
    public void onPause() {
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
        if(!solved&&!keyguardManager.isKeyguardLocked()) {
            Intent intent = new Intent(this, AlarmActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("h", h);
            intent.putExtra("m", m);
            intent.putExtra("sound", sound);
            intent.putExtra("vibration", vibration);
            intent.putExtra("problem_id", problem.id);
            intent.putExtra("problem_type", problem_type);
            startActivity(intent);

            // finish this activity
            finish();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
        if(!solved) {
            Intent intent = new Intent(this, AlarmActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("h", h);
            intent.putExtra("m", m);
            intent.putExtra("sound", sound);
            intent.putExtra("vibration", vibration);
            intent.putExtra("problem_id", problem.id);
            intent.putExtra("problem_type", problem_type);
            startActivity(intent);

            // finish this activity
            finish();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }


    SoundPool soundPool;
    ArrayList<Integer> streamIDs = new ArrayList<>();
    public void playSound() {
        try {
            Log.d("playSound", "playSound");
            closePlayer();
            soundPool = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()).build();
            soundPool.load(getApplicationContext(), R.raw.alarm_sound, 1);
            soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
                streamIDs.add(soundPool.play(sampleId, 1, 1, 0, -1, 1));
                Log.d("playSound", streamIDs.toString() + "");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopSound() {
        try {
            closePlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePlayer(){
        if (soundPool != null) {
            for (int streamID : streamIDs) {
                soundPool.stop(streamID);
            }
            soundPool.release();
            soundPool = null;
        }
    }

    void coolDown() {
        TextView coolDownTextView = findViewById(R.id.coolDownTextView);
        coolDownTextView.setVisibility(TextView.VISIBLE);
        onCoolDown = true;
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                coolDownTextView.setText((millisUntilFinished / 1000) + "초 후 다시 답을 골라주세요.");
            }

            @Override
            public void onFinish() {
                coolDownTextView.setVisibility(TextView.GONE);
                onCoolDown = false;
            }
        }.start();
    }
}