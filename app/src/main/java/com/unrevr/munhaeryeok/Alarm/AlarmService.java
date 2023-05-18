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
        Intent intent;
        // 1: 객관, 2: 주관, 3: 전부
        if (problem_type == 1) intent = new Intent(this, AlarmMCActivity.class);
        else if(problem_type == 2) intent = new Intent(this, AlarmSFActivity.class);
        else {
            if ((int) (Math.random() * 2) == 0) intent = new Intent(this, AlarmMCActivity.class);
            else intent = new Intent(this, AlarmSFActivity.class);
        }

        intent.putExtra("id", id);
        intent.putExtra("h", h);
        intent.putExtra("m", m);
        intent.putExtra("sound", sound);
        intent.putExtra("vibration", vibration);
        intent.putExtra("name", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
