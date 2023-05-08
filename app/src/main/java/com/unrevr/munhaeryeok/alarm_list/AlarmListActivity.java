package com.unrevr.munhaeryeok.alarm_list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.AlarmData;
import com.unrevr.munhaeryeok.AlarmSettingActivity;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class AlarmListActivity extends AppCompatActivity {
    AlarmListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_list_layout);

        // RecyclerView
        setRecyclerView();

        // Buttons
        findViewById(R.id.backwardButton).setOnClickListener(v -> {
            finish();
        });
        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, AlarmSettingActivity.class);
            startActivity(intent);
        });
    }

    void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.alarmRecyclerView);
        recyclerView.setAdapter(null);
        SharedPreferences pref = getSharedPreferences("alarm_data", MODE_PRIVATE);
        String original = pref.getString("alarms_list", "").trim();
        ArrayList<AlarmData> alarmDataList = new ArrayList<>();
        if(original != "") {
            String[] list = original.split("\n");
            for (String s : list) {
                alarmDataList.add(new AlarmData(s));
            }
            adapter = new AlarmListAdapter(alarmDataList);
        }
        else {
            adapter = new AlarmListAdapter(new ArrayList<AlarmData>());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(this, AlarmSettingActivity.class);
            intent.putExtra("id", alarmDataList.get(position).id);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerView();
    }
}
