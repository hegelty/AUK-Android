package com.unrevr.munhaeryeok.Alarm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/** Entered by the user when saving an alarm or tapping the pending-permission banner. */
final class AlarmAccess {
    private final AppCompatActivity activity;
    private final AlarmController alarms;
    private final Runnable finished;
    private final ActivityResultLauncher<String> notification;
    private final ActivityResultLauncher<Intent> exactSettings, notificationSettings;

    AlarmAccess(AppCompatActivity activity, AlarmController alarms, Runnable finished) {
        this.activity = activity;
        this.alarms = alarms;
        this.finished = finished;
        exactSettings = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> complete());
        notificationSettings = activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (alarms.notificationsAllowed()) request(); else complete();
                });
        notification = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) request();
                    else showNotificationSettings();
                });
    }

    void request() {
        if (!alarms.notificationsAllowed()) {
            if (Build.VERSION.SDK_INT >= 33 && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                explain("알람이 울릴 때 알림을 표시할 수 있도록 허용해주세요.",
                        () -> notification.launch(Manifest.permission.POST_NOTIFICATIONS));
            } else {
                showNotificationSettings();
            }
        } else if (!alarms.exactAllowed() && Build.VERSION.SDK_INT >= 31) {
            explain("설정한 시각에 울리려면 ‘알람 및 리마인더’를 허용해주세요.",
                    () -> launch(exactSettings, new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:" + activity.getPackageName()))));
        } else {
            complete();
        }
    }

    private void showNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
        explain("알람 알림이 꺼져 있습니다. 설정에서 앱 알림과 알람 채널을 켜주세요.", () -> launch(notificationSettings, intent));
    }

    private void launch(ActivityResultLauncher<Intent> launcher, Intent intent) {
        try {
            launcher.launch(intent);
        } catch (ActivityNotFoundException e) {
            complete();
        }
    }

    private void explain(String message, Runnable positive) {
        new AlertDialog.Builder(activity)
                .setTitle("알람 권한 필요").setMessage(message)
                .setPositiveButton("계속", (dialog, which) -> positive.run())
                .setNegativeButton("나중에", (dialog, which) -> complete())
                .setOnCancelListener(dialog -> complete()).show();
    }

    private void complete() {
        alarms.refreshAlarms();
        finished.run();
    }
}
