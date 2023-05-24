package com.unrevr.munhaeryeok.Alarm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.unrevr.munhaeryeok.R;

public class WrongAlarmsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wrong_alarms_list_layout);

        findViewById(R.id.backwardButton).setOnClickListener(v -> {
            finish();
        });
    }
}