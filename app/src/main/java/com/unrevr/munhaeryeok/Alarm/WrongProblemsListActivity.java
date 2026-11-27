package com.unrevr.munhaeryeok.Alarm;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;
import com.unrevr.munhaeryeok.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class WrongProblemsListActivity extends AppCompatActivity {
    private final WrongProblemsListAdaptor adapter = new WrongProblemsListAdaptor();
    private int problemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_problem_list_layout);
        problemType = savedInstanceState == null ? 0 : savedInstanceState.getInt("problem_type");
        RecyclerView recyclerView = findViewById(R.id.wrongProblemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());
        findViewById(R.id.allButton).setOnClickListener(v -> showType(0));
        findViewById(R.id.MCButton).setOnClickListener(v -> showType(Problem.MC));
        findViewById(R.id.SFButton).setOnClickListener(v -> showType(Problem.SF));
    }

    private void showType(int type) {
        problemType = type;
        setSelected(R.id.allButton, type == 0);
        setSelected(R.id.MCButton, type == Problem.MC);
        setSelected(R.id.SFButton, type == Problem.SF);
        adapter.showType(type);
    }

    private void setSelected(int button, boolean selected) {
        findViewById(button).setBackgroundResource(selected
                ? R.drawable.round_corner_blue : R.drawable.round_corner_black);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserInfo user = UserInfo.getInstance(this);
        List<Problem> problems = new ArrayList<>();
        for (int type : new int[]{Problem.SF, Problem.MC}) {
            for (int id : user.getWrongProblems(type)) {
                if (Problem.hasId(this, type, id)) problems.add(Problem.byId(this, type, id));
            }
        }
        adapter.setProblems(problems);
        showType(problemType);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("problem_type", problemType);
    }
}
