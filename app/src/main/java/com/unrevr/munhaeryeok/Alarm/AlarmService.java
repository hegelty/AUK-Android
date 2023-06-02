package com.unrevr.munhaeryeok.Alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {
    public AlarmService() {}

    @Override
    public IBinder onBind(Intent intent) {
            return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getIntExtra("id", 0);
        int h = intent.getIntExtra("h", 0);
        int m = intent.getIntExtra("m", 0);
        boolean sound = intent.getBooleanExtra("sound", false);
        boolean vibration = intent.getBooleanExtra("vibration", false);
        String name = intent.getStringExtra("name");
        int problem_type = intent.getIntExtra("problem_type", 0);

        showAlarm(id, h, m, sound, vibration, name, problem_type);
        return START_REDELIVER_INTENT; // 서비스가 종료되었을 때, 자동으로 재시작
    }

    private void showAlarm(int id, int h, int m, boolean sound, boolean vibration, String name, int problem_type) {
        Intent intent = new Intent(this, AlarmActivity.class);
        // 1: 객관, 2: 주관, 3: 전부
        if (problem_type == 1) intent.putExtra("problem_type", 1);
        else if(problem_type == 2) intent.putExtra("problem_type", 2);
        else intent.putExtra("problem_type", (int) (Math.random() * 2) + 1);
        intent.putExtra("id", id);
        intent.putExtra("h", h);
        intent.putExtra("m", m);
        intent.putExtra("name", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Intent soundIntent = new Intent(this, SoundService.class);
        soundIntent.putExtra("sound", sound);
        soundIntent.putExtra("vibration", vibration);
        startService(soundIntent);
    }
}
