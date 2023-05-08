package com.unrevr.munhaeryeok.Alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;

public class AlarmMCActivity extends AppCompatActivity {
    int id, h, m;
    boolean sound, vibration;
    Problem problem;
    int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_mc_layout);
        Intent intent = getIntent();

        id = intent.getIntExtra("id", 0);
        h = intent.getIntExtra("h", 0);
        m = intent.getIntExtra("m", 0);
        sound = intent.getBooleanExtra("sound", false);
        vibration = intent.getBooleanExtra("vibration", false);
        problem = new Problem(Problem.MC);

        cnt = 0;

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

    }

    void addCorrectProblem(Problem problem) {

    }
}