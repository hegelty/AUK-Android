package com.unrevr.munhaeryeok;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import com.unrevr.munhaeryeok.alarm_list.AlarmListActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFirstRun();
        getOverlayPermission();

        findViewById(R.id.upcommingAlarm).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AlarmListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.settingButton).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
        });
    }

    void checkFirstRun() {
        SharedPreferences pref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        boolean first = pref.getBoolean("isFirst", true);

        if(first) {
            editor.putBoolean("isFirst", true);
            editor.apply();

            DataController dataCon = new DataController(getApplicationContext(), "alarm_data");
            dataCon.putInt("last_alarm_id", 0);
            dataCon.putString("alarm_list", "");
        }
    }

    void getOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            return;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("권한이 필요합니다.").setMessage("다른 앱 위에 그리기 권한이 필요합니다. 설정 화면으로 이동하시겠습니까?");
            builder.setPositiveButton("예", (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
            ).setNegativeButton("아니오", (dialog, id) -> finish()).setCancelable(false);
            builder.create().show();
        }
    }
}