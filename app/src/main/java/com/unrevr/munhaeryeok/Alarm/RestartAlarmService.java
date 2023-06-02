package com.unrevr.munhaeryeok.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.unrevr.munhaeryeok.DataController;

public class RestartAlarmService extends Service {
    public RestartAlarmService() {}

    @Override
    public IBinder onBind(Intent intent) {
            return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Reboot", "Reboot");
        Notification notification = new Notification.Builder(getApplicationContext(), "alarm")
                .setContentTitle("AUK")
                .setContentText("AUK 알람을 설정 중입니다.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build();
        startForeground(1, notification);

        DataController dataCon = new DataController(getApplicationContext(),"alarm_data");
        String alarms_list = dataCon.getString("alarms_list", "").trim();
        String[] list = alarms_list.split("=");

        for(String s: list) {
            if(s!=null) {
                AlarmData alarmData = new AlarmData(s);
                Log.d("MainActivity", "reload: " + alarmData.toString());
                for(int id: alarmData.alarm_ids) {
                    if(id!=0) {
                        AlarmController alarmController = new AlarmController(getApplicationContext());
                        alarmController.reloadAlarms(id);
                    }
                }
            }
        }
        return START_REDELIVER_INTENT; // 서비스가 종료되었을 때, 자동으로 재시작
    }
}
