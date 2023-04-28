package com.unrevr.munhaeryeok.Alarm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.unrevr.munhaeryeok.R;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_layout);
        String text = getIntent().getStringExtra("text");
        TextView tv = findViewById(R.id.tv1);
        tv.setText(text);
    }
}