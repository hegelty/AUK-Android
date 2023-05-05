package com.unrevr.munhaeryeok.alarm_list;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.AlarmData;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class AlarmListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_list_layout);

        RecyclerView recyclerView = findViewById(R.id.alarmRecyclerView);
        AlarmListAdapter adapter = new AlarmListAdapter();

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showAlarmList(adapter);
    }


    void showAlarmList(AlarmListAdapter adapter) {SharedPreferences pref = getSharedPreferences("alarm_data", MODE_PRIVATE);
        String original = pref.getString("alarms_list", "").trim();
        Log.d("debug", original);
        if(original != "") {
            String[] list = original.split("\n");
            ArrayList<AlarmData> alarmDataList = new ArrayList<>();
            for (String s : list) {
                Log.d("show", s);
                alarmDataList.add(new AlarmData(s));
            }
            adapter.setAlarmItems(alarmDataList);
            adapter.notifyDataSetChanged();
        }
        adapter.setAlarmItems(new ArrayList<AlarmData>());
    }
}
