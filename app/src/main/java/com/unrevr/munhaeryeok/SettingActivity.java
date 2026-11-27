package com.unrevr.munhaeryeok;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);

        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());
        findViewById(R.id.helpButton).setOnClickListener(v -> openLink("https://open.kakao.com/o/gObXhsif"));
        findViewById(R.id.developersLayout).setOnClickListener(v -> showInfo("개발자", R.string.developers));
        findViewById(R.id.licenseButton).setOnClickListener(v -> showInfo("오픈소스 라이선스", R.string.opensource_licence));
        findViewById(R.id.policyButton).setOnClickListener(v -> showPrivacyInfo());

        SwitchCompat shareScore = findViewById(R.id.shareScore);
        shareScore.setChecked(ScoreSharing.isEnabled(this));
        shareScore.setOnCheckedChangeListener((button, enabled) -> ScoreSharing.setEnabled(this, enabled));
    }

    private void showInfo(String title, int content) {
        Intent intent = new Intent(this, SettingInfoActivity.class);
        intent.putExtra("content", getString(content));
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private void showPrivacyInfo() {
        UserInfo user = UserInfo.getInstance(getApplicationContext());
        user.getUserInfo();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.privacy_title)
                .setMessage(getString(R.string.privacy_summary, user.id.trim()))
                .setPositiveButton(R.string.privacy_full, (d, which) ->
                        openLink("https://auk.hegelty.space/policy/index.html"))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        TextView message = dialog.findViewById(android.R.id.message);
        if (message != null) message.setTextIsSelectable(true);
    }

    private void openLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show();
        }
    }
}
