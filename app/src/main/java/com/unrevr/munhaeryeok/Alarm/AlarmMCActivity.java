package com.unrevr.munhaeryeok.Alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class AlarmMCActivity extends AppCompatActivity {
    int id, h, m;
    boolean sound, vibration;
    Problem problem;
    int cnt;
    boolean solved;

    Vibrator vibrator;
    KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.alarm_mc_layout);
        Intent intent = getIntent();

        Log.d("AlarmMCActivity", "onCreate");

        setTurnScreenOn(true);
        setShowWhenLocked(true);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        solved = false;

        id = intent.getIntExtra("id", 0);
        h = intent.getIntExtra("h", 0);
        m = intent.getIntExtra("m", 0);
        sound = intent.getBooleanExtra("sound", false);
        vibration = intent.getBooleanExtra("vibration", false);

        int problem_id = intent.getIntExtra("problem_id", 0);
        if(problem_id!=0) problem = new Problem(Problem.MC, problem_id);
        else problem = new Problem(Problem.MC);

        cnt = 0;

        if (sound) playSound();
        // vibrate until destroyed
        if (vibration) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = {0, 1000, 500};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
        }


        initLayout();
    }

    void initLayout() {
        TextView timeTextView = findViewById(R.id.timeTextView);
        TextView AMPMTextView = findViewById(R.id.AMPMTextView);
        TextView questionTextView = findViewById(R.id.questionTextView);
        TextView hintTextView = findViewById(R.id.hintTextView);
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

        Button closeButton = findViewById(R.id.closeButton);
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

        for (int i = 0; i < 4; i++) {
            answerTextView[i].setText(problem.mcAnswers[i]);
            int I = i;
            answerLayout[i].setOnClickListener(v -> {
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
                    }
                }
            });
        }

        closeButton.setOnClickListener(v -> {
            finish();
        });
    }

    void addWrongProblem(Problem problem) {
        solved = true;
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
    }

    void addCorrectProblem(Problem problem) {
        solved = true;
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
    }

    // 강제로 끌때 동작
    @Override
    public void onPause() {
        if(sound) stopSound();
        if(vibration) vibrator.cancel();
        super.onPause();
        if(!solved&&!keyguardManager.isKeyguardLocked()) {
            Intent intent = new Intent(this, AlarmMCActivity.class);
            intent.putExtra("id", id);
            intent.putExtra("h", h);
            intent.putExtra("m", m);
            intent.putExtra("sound", sound);
            intent.putExtra("vibration", vibration);
            intent.putExtra("problem_id", problem.id);
            startActivity(intent);

            solved = true;

            // finish this activity
            finish();
        }
    }

    @Override
    public void onDestroy() {
        if(sound) stopSound();
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
}