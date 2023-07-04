package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.unrevr.munhaeryeok.DataController;
import com.unrevr.munhaeryeok.R;

public class RestartAlarmService extends Service {
    public RestartAlarmService() {}

    @Override
    public IBinder onBind(Intent intent) {
            return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Reboot", "Reboot");
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("tmp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("test", true);
        editor.apply();

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), AlarmListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(getApplicationContext(), "alarm")
                .setContentTitle("AUK")
                .setContentText("알람을 설정했습니다.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        DataController dataCon = new DataController(getApplicationContext(),"alarm_data");
        String alarms_list = dataCon.getString("alarms_list", "").trim();
        String[] list = alarms_list.split("=");

        AlarmController alarmController = new AlarmController(getApplicationContext());
        alarmController.reloadAlarms();

        return START_REDELIVER_INTENT; // 서비스가 종료되었을 때, 자동으로 재시작
    }
}
