package com.unrevr.munhaeryeok.Alarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class WrongProblemsListActivity extends AppCompatActivity {
    WrongProblemsListAdaptor adapter;
    int problemType = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_problem_list_layout);

        setRecyclerView();

        // Buttons
        findViewById(R.id.backwardButton).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.allButton).setOnClickListener(v -> {
            if(problemType != 0) {
                problemType = 0;
                findViewById(R.id.SFButton).setBackgroundResource(R.drawable.round_corner_black);
                findViewById(R.id.MCButton).setBackgroundResource(R.drawable.round_corner_black);
                findViewById(R.id.allButton).setBackgroundResource(R.drawable.round_corner_blue);
                adapter.getFilter().filter("1");
            }
        });
        findViewById(R.id.SFButton).setOnClickListener(v -> {
            if(problemType != Problem.SF) {
                problemType = Problem.SF;
                findViewById(R.id.SFButton).setBackgroundResource(R.drawable.round_corner_blue);
                findViewById(R.id.MCButton).setBackgroundResource(R.drawable.round_corner_black);
                findViewById(R.id.allButton).setBackgroundResource(R.drawable.round_corner_black);
                adapter.getFilter().filter(Problem.SF + "");
            }
        });
        findViewById(R.id.MCButton).setOnClickListener(v -> {
            if(problemType != Problem.MC) {
                problemType = Problem.MC;
                findViewById(R.id.SFButton).setBackgroundResource(R.drawable.round_corner_black);
                findViewById(R.id.MCButton).setBackgroundResource(R.drawable.round_corner_blue);
                findViewById(R.id.allButton).setBackgroundResource(R.drawable.round_corner_black);
                adapter.getFilter().filter(Problem.MC + "");
            }
        });
    }

    void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.wrongProblemsRecyclerView);
        recyclerView.setAdapter(null);
        UserInfo userInfo = UserInfo.getInstance(getApplicationContext());
        List<Integer> wrongSFProblems = userInfo.getWrongProblems(Problem.SF);
        List<Integer> wrongMCProblems = userInfo.getWrongProblems(Problem.MC);

        ArrayList<Problem> problemList = new ArrayList<>();
        for(int i = 0; i < wrongSFProblems.size(); i++) {
            problemList.add(new Problem(Problem.SF, wrongSFProblems.get(i)));
        }
        for(int i = 0; i < wrongMCProblems.size(); i++) {
            problemList.add(new Problem(Problem.MC, wrongMCProblems.get(i)));
        }

        adapter = new WrongProblemsListAdaptor(problemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecyclerView();
        adapter.getFilter().filter(problemType + "");
    }
}