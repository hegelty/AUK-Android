package com.unrevr.munhaeryeok.Alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.Problem;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class WrongProblemsListAdaptor extends RecyclerView.Adapter<WrongProblemsListAdaptor.ViewHolder> implements Filterable {
    private ArrayList<Problem> problemItems;
    private ArrayList<Problem> problemItemsFull;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();
                if(query.equals("0")) {
                    problemItems = problemItemsFull;
                } else if (query.equals(Problem.SF + "")) {
                    problemItems = problemItemsFull;
                    ArrayList<Problem> filteredList = new ArrayList<>();
                    for(Problem problem : problemItems) {
                        if(problem.problemType == Integer.parseInt(query)) {
                            filteredList.add(problem);
                        }
                    }
                    problemItems = filteredList;
                } else {
                    problemItems = problemItemsFull;
                    ArrayList<Problem> filteredList = new ArrayList<>();
                    for(Problem problem : problemItems) {
                        if(problem.problemType == Integer.parseInt(query)) {
                            filteredList.add(problem);
                        }
                    }
                    problemItems = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = problemItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                problemItems = (ArrayList<Problem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    WrongProblemsListAdaptor(ArrayList<Problem> problemItems) {
        this.problemItems = problemItems;
        problemItemsFull = new ArrayList<>(problemItems);
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
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
