package com.unrevr.munhaeryeok.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.unrevr.munhaeryeok.DataController;
import com.unrevr.munhaeryeok.R;

public class RestartAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: service로 구현하기
        AlarmController alarmController = new AlarmController(context.getApplicationContext());
        DataController dataCon = new DataController(context.getApplicationContext(),"alarm_data");
        String alarms_list = dataCon.getString("alarms_list", "").trim();
        if(alarms_list.equals("")) return;

        String[] list = alarms_list.split("=");
        String new_list = "";
        for (String s : list) {
            if(s!=null) {
                AlarmData alarmData = new AlarmData(s);
                int[] alarm_ids = {0, 0, 0, 0, 0, 0, 0};
                for(int i=0;i<7;i++) {
                    alarm_ids[i] = alarmController.setAlarmAgain(alarmData.alarm_ids[i]);
                }
                alarmData.alarm_ids = alarm_ids;
                new_list += alarmData.toString() + "=";
            }
        }
        dataCon.putString("alarms_list", new_list);
    }
}
