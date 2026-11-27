package com.unrevr.munhaeryeok.Alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WrongProblemsListAdaptor extends RecyclerView.Adapter<WrongProblemsListAdaptor.ViewHolder> {
    private final List<Problem> problemItems = new ArrayList<>();
    private List<Problem> allProblems = Collections.emptyList();

    void setProblems(List<Problem> problems) {
        allProblems = problems;
    }

    void showType(int type) {
        problemItems.clear();
        for (Problem problem : allProblems) {
            if (type == 0 || type == problem.problemType) problemItems.add(problem);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WrongProblemsListAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wrong_problem_list_item, parent, false);
        return new WrongProblemsListAdaptor.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WrongProblemsListAdaptor.ViewHolder holder, int position) {
        holder.onBind(problemItems.get(position));
    }

    @Override
    public int getItemCount() {
        return problemItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView problemTitle;
        TextView problemTextView;
        TextView answerTextView;
        ViewHolder(@NonNull View itemView) {
            super(itemView);

            problemTitle = itemView.findViewById(R.id.problemTitle);
            problemTextView = itemView.findViewById(R.id.problemTextView);
            answerTextView = itemView.findViewById(R.id.answerTextView);

        }

        void onBind(Problem problem) {
            switch (problem.problemType) {
                case Problem.MC:
                    problemTitle.setText(problem.question);
                    String answers = "";
                    int i = 1;
                    for(String answer : problem.mcAnswers) {
                        answers += (i++) + ". " + answer + "\n";
                    }
                    problemTextView.setText(answers);
                    answerTextView.setText("답: " + problem.mcCorrectAnswer + "\n" + problem.solution);
                    break;
                case Problem.SF:
                    problemTitle.setText("빈칸에 들어갈 알맞은 말은?");
                    problemTextView.setText(problem.question);
                    answerTextView.setText("답: " + problem.sfAnswer + "\n" + problem.solution);
                    break;
            }
        }
    }
}
