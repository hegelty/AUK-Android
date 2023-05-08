package com.unrevr.munhaeryeok.Alarm;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unrevr.munhaeryeok.R;

public class AlarmSFActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_sf_layout);
        String text = getIntent().getStringExtra("text");
        TextView tv = findViewById(R.id.tv1);
        tv.setText(text);
    }
}