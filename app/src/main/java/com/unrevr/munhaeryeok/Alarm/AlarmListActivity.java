package com.unrevr.munhaeryeok.Alarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.DataController;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class AlarmListActivity extends AppCompatActivity {
    AlarmListAdapter adapter;
    Boolean isFavorite = false;
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
        findViewById(R.id.favoriteButton).setOnClickListener(v -> {
            if(!isFavorite) {
                isFavorite = true;
                findViewById(R.id.favoriteButton).setBackgroundResource(R.drawable.round_corner_blue);
                findViewById(R.id.allButton).setBackgroundResource(R.drawable.round_corner_black);
                adapter.getFilter().filter("1");
            }
        });
        findViewById(R.id.allButton).setOnClickListener(v -> {
            if(isFavorite) {
                isFavorite = false;
                findViewById(R.id.favoriteButton).setBackgroundResource(R.drawable.round_corner_black);
                findViewById(R.id.allButton).setBackgroundResource(R.drawable.round_corner_blue);
                adapter.getFilter().filter("0");
            }
        });
    }

    void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.alarmRecyclerView);
        recyclerView.setAdapter(null);
        DataController dataCon = new DataController(getApplicationContext(),"alarm_data");
        String original = dataCon.getString("alarms_list", "").trim();
        Log.d("AlarmListActivity", original);
        ArrayList<AlarmData> alarmDataList = new ArrayList<>();
        if(original != "") {
            String[] list = original.split("=");
            for (String s : list) {
                Log.d("AlarmListActivity", s);
                if(s!=null) alarmDataList.add(new AlarmData(s));
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
        adapter.getFilter().filter(isFavorite ? "1" : "0");
    }
}
