package com.unrevr.munhaeryeok;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DataController {
    private String name;
    private Context context;
    public DataController(Context context, String name) {
        this.context = context;
        this.name = name;
    }

    public void putString(String key, String value) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name + "_" + key, Context.MODE_PRIVATE));
            outputStreamWriter.write(value);
            outputStreamWriter.close();
        } catch (Exception ignored) {}
    }

    public String getString(String key, String defaultValue) {
        try {
            InputStream inputStream = context.openFileInput(name + "_" + key);
            if (inputStream!=null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String result = "";
                while (bufferedReader.readLine() != null) {
                    result += "\n" + bufferedReader.readLine();
                }
                inputStreamReader.close();
                inputStream.close();
                return result;
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }

    public void putInt(String key, int value) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name + "_" + key, Context.MODE_PRIVATE));
            outputStreamWriter.write(value +"");
            outputStreamWriter.close();
        } catch (Exception ignored) {}
    }

    public int getInt(String key, int defaultValue) {
        try {
            InputStream inputStream = context.openFileInput(name + "_" + key);
            if (inputStream!=null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                int result = Integer.parseInt(bufferedReader.readLine());
                inputStreamReader.close();
                inputStream.close();
                return result;
            }
        } catch (Exception ignored) {}
        return defaultValue;
    }
}