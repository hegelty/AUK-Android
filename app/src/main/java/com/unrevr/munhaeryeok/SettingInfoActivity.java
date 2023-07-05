package com.unrevr.munhaeryeok;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_info_layout);

        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());

        ((TextView)findViewById(R.id.contentText)).setText(getIntent().getStringExtra("content"));
        ((TextView)findViewById(R.id.titleText)).setText(getIntent().getStringExtra("title"));
    }
}
