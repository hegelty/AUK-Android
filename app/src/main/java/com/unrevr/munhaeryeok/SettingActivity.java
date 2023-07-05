package com.unrevr.munhaeryeok;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());
        findViewById(R.id.helpButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://open.kakao.com/o/gObXhsif"));
            startActivity(intent);
        });

        findViewById(R.id.developersLayout).setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, SettingInfoActivity.class);
            intent.putExtra("content", getString(R.string.developers));
            intent.putExtra("title", "개발자");
            startActivity(intent);
        });

        findViewById(R.id.licenseButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, SettingInfoActivity.class);
            intent.putExtra("content", getString(R.string.opensource_licence));
            intent.putExtra("title", "오픈소스 라이선스");
            startActivity(intent);
        });

        findViewById(R.id.policyButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://auk.hegelty.space/policy/index.html"));
            startActivity(intent);
        });
    }
}
