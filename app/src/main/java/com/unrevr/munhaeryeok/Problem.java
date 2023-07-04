package com.unrevr.munhaeryeok;

import android.util.Log;

import java.io.InputStream;

public class Problem {
    public String question;
    public int problemType;
    public int id;
    public String[] mcAnswers;
    public int mcCorrectAnswer;
    public String sfAnswer;
    public String hint;
    public String solution;

    public static final int MC = 1;
    public static final int SF = 2;

    public Problem(int type) {
        problemType = type;
        id = 0;
        if(type == 1) getMCProblem();
        else if(type == 2) getSFProblem();
    }

    public Problem(int type, int id) {
        problemType = type;
        this.id = id;
        if(type == 1) getMCProblem();
        else if(type == 2) getSFProblem();
    }

    void getMCProblem() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/raw/mc");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String str = new String(buffer);
            is.close();
            String[] problems = str.split(";");
            if(id==0) id = (int)(Math.random()*problems.length);
            Log.d("id", id+"-"+problems.length);
            String[] problem = problems[id].trim().split("\\|");
            question = problem[0];
            mcAnswers = new String[4];
            for(int i=0;i<4;i++) mcAnswers[i] = problem[i+1];
            mcCorrectAnswer = Integer.parseInt(problem[5]);
            hint = problem[6];
            solution = problem[7];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getSFProblem() {
        try {
            InputStream is = getClass().getResourceAsStream("/res/raw/sf");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            String str = new String(buffer);
            is.close();
            String[] problems = str.split(";");
            if(id==0) id = (int)(Math.random()*problems.length);
            String[] problem = problems[id].trim().split("\\|");
            question = problem[0];
            sfAnswer = problem[1];
            hint = problem[2];
            solution = problem[3];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkAnswer(int answer) {
        if(problemType == MC) return answer == mcCorrectAnswer - 1;
        else return false;
    }

    public boolean checkAnswer(String answer) {
        if(problemType == SF) return answer.trim().equals(sfAnswer);
        else return false;
    }
}
