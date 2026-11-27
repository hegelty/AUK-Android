package com.unrevr.munhaeryeok;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Problem {
    public static final int MC = 1;
    public static final int SF = 2;

    public final int problemType;
    public final int id;
    public final String question;
    public final String[] mcAnswers;
    public final int mcCorrectAnswer;
    public final String sfAnswer;
    public final String hint;
    public final String solution;

    private static List<Problem> multipleChoice;
    private static List<Problem> shortForm;

    private Problem(int type, int index, String[] fields) {
        problemType = type;
        id = index;
        question = fields[0];
        mcAnswers = type == MC ? Arrays.copyOfRange(fields, 1, 5) : null;
        mcCorrectAnswer = type == MC ? Integer.parseInt(fields[5]) : 0;
        sfAnswer = type == SF ? fields[1] : null;
        hint = fields[fields.length - 2];
        solution = fields[fields.length - 1];
    }

    public static Problem random(Context context, int type) {
        List<Problem> problems = catalog(context, type);
        return problems.get(ThreadLocalRandom.current().nextInt(problems.size()));
    }

    public static Problem byId(Context context, int type, int id) {
        return catalog(context, type).get(id);
    }

    public static boolean hasId(Context context, int type, int id) {
        return id >= 0 && id < catalog(context, type).size();
    }

    static synchronized List<Problem> catalog(Context context, int type) {
        if (type == MC) {
            if (multipleChoice == null) multipleChoice = load(context, type, R.raw.mc);
            return multipleChoice;
        }
        if (type == SF) {
            if (shortForm == null) shortForm = load(context, type, R.raw.sf);
            return shortForm;
        }
        throw new IllegalArgumentException("Unknown problem type " + type);
    }

    private static List<Problem> load(Context context, int type, int resource) {
        try (InputStream input = context.getResources().openRawResource(resource)) {
            return parse(type, input);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load bundled problems", e);
        }
    }

    static List<Problem> parse(int type, InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) contents.append(line).append('\n');
        ArrayList<Problem> problems = new ArrayList<>();
        for (String record : contents.toString().trim().split(";")) {
            String[] fields = record.trim().split("\\|", -1);
            if (fields.length != (type == MC ? 8 : 4)) {
                throw new IllegalArgumentException("Malformed bundled problem " + problems.size());
            }
            Problem problem = new Problem(type, problems.size(), fields);
            if (type == MC && (problem.mcCorrectAnswer < 1 || problem.mcCorrectAnswer > 4)) {
                throw new IllegalArgumentException("Invalid answer number at " + problem.id);
            }
            problems.add(problem);
        }
        return Collections.unmodifiableList(problems);
    }

    public boolean checkAnswer(int answer) {
        return problemType == MC && answer == mcCorrectAnswer - 1;
    }

    public boolean checkAnswer(String answer) {
        return problemType == SF && answer != null && answer.trim().equals(sfAnswer);
    }
}
