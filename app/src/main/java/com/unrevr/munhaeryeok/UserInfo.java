package com.unrevr.munhaeryeok;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class UserInfo {
    private Context context;

    public static int score;
    public static int accuracy;
    public static int solvedCount;
    public static int wrongCount;
    public static List<Integer> wrongSFProblems;
    public static List<Integer> wrongMCProblems;

    private DataController dataController;

    UserInfo(Context context) {
        this.context = context;
    }

    public static UserInfo getInstance(Context context) {
        return new UserInfo(context);
    }

    public void getUserInfo() {
        dataController = new DataController(context,"user_info");

        score = dataController.getInt("score", 0);

        String[] wrongSFProblems_s = dataController.getString("wrong_sf_problems", "").split(",");
        wrongSFProblems = new LinkedList<>();
        for (String wrongSFProblems_ : wrongSFProblems_s) {
            if(!Objects.equals(wrongSFProblems_, "")) wrongSFProblems.add(Integer.parseInt(wrongSFProblems_));
        }

        String[] wrongMCProblems_s = dataController.getString("wrong_mc_problems", "").split(",");
        wrongMCProblems = new LinkedList<>();
        for (String wrongMCProblems_ : wrongMCProblems_s) {
            if(!Objects.equals(wrongMCProblems_, "")) wrongMCProblems.add(Integer.parseInt(wrongMCProblems_));
        }

        accuracy = dataController.getInt("accuracy", 0);
        solvedCount = dataController.getInt("solved_count", 0);
        wrongCount = dataController.getInt("wrong_count", 0);
    }

    public int solve() {
        dataController = new DataController(context,"user_info");
        solvedCount++;
        accuracy = solvedCount / (solvedCount + wrongCount) * 100;
        score =  accuracy / 2 + (Math.min(solvedCount, 200)) / 4;
        dataController.putInt("score", score);
        dataController.putInt("solved_count", solvedCount);

        return score;
    }

    public int wrong(int id, int type) { // MC: 1, SF: 2
        dataController = new DataController(context,"user_info");
        wrongCount++;
        accuracy = solvedCount / (solvedCount + wrongCount) * 100;
        score =  accuracy / 2 + (Math.min(solvedCount, 200)) / 4;
        dataController.putInt("score", score);
        dataController.putInt("wrong_count", wrongCount);

        if(type == 1) {
            if(!wrongMCProblems.contains(id)) wrongMCProblems.add(id);
            dataController.putString("wrong_mc_problems", wrongMCProblems.toString());
        }
        else {
            if(!wrongSFProblems.contains(id)) wrongSFProblems.add(id);
            dataController.putString("wrong_sf_problems", wrongSFProblems.toString());
        }

        return score;
    }
}
