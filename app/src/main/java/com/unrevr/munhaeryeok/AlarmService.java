package com.unrevr.munhaeryeok;

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
        showAlarm();
        return START_REDELIVER_INTENT; // 서비스가 종료되었을 때, 자동으로 재시작
    }

    private void showAlarm() {
        Intent intent = new Intent(this, AlarmActivity.class);
        intent.putExtra("text", "알람");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
