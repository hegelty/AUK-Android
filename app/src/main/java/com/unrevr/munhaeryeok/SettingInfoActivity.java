package com.unrevr.munhaeryeok;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SettingInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_info_layout);

        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());

        TextView content = findViewById(R.id.contentText);
        if (getIntent().getBooleanExtra("privacy", false)) {
            content.setText(readPolicy());
            content.setTextIsSelectable(true);
        } else {
            content.setText(getIntent().getStringExtra("content"));
        }
        ((TextView)findViewById(R.id.titleText)).setText(getIntent().getStringExtra("title"));
    }

    private CharSequence readPolicy() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getAssets().open("privacy.html"), StandardCharsets.UTF_8))) {
            StringBuilder html = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) html.append(line).append('\n');
            return Html.fromHtml(html.toString(), Html.FROM_HTML_MODE_COMPACT);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bundled privacy notice", e);
        }
    }
}
