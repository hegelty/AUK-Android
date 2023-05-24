package com.unrevr.munhaeryeok;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.unrevr.munhaeryeok.Alarm.AlarmListActivity;
import com.unrevr.munhaeryeok.Alarm.WrongAlarmsListActivity;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static Context context;
    private boolean loadDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.main_activity_layout);

        checkFirstRun();
        getOverlayPermission();
        updateAppIfAvailable();
        setProgressBar();

        findViewById(R.id.upcommingAlarm).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AlarmListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.moreText).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AlarmListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.settingButton).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.progressBar).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), WrongAlarmsListActivity.class);
            startActivity(intent);
        });

        setTodayContents();

        // splash screen
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check if the initial data is ready.
                        if (loadDone) {
                            // The content is ready; start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            // The content is not ready; suspend.
                            return false;
                        }
                    }
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
            if(dataCon.getInt("last_alarm_id", 0) == 0) {
                dataCon.putInt("last_alarm_id", 0);
                dataCon.putString("alarm_list", "");
            }
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

    void updateAppIfAvailable() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    void setProgressBar() {
        UserInfo userInfo = new UserInfo(getApplicationContext());
        userInfo.getUserInfo();
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressText = findViewById(R.id.progressText);

        progressBar.setProgress(UserInfo.score);
        progressText.setText(UserInfo.score + " / 100");
    }

    void setTodayContents() {
        AtomicBoolean contents1Loaded = new AtomicBoolean(false);
        AtomicBoolean contents2Loaded = new AtomicBoolean(false);

        TextView todayTitle1 = findViewById(R.id.todayTitle1);
        TextView todayTitle2 = findViewById(R.id.todayTitle2);
        TextView author1 = findViewById(R.id.author1);
        TextView author2 = findViewById(R.id.author2);
        TextView todayContent1 = findViewById(R.id.todayContent1);
        TextView todayContent2 = findViewById(R.id.todayContent2);
        Button readButton1 = findViewById(R.id.readButton1);
        Button readButton2 = findViewById(R.id.readButton2);

        Disposable backgroundtask = null;
        if(isNetworkAvailable()) {
            backgroundtask = Observable.fromCallable(() -> {
                TodayContents todayContents = new TodayContents();
                todayContents.getColumn(0);

                return todayContents;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((todayContents) -> {
                        todayTitle1.setText(todayContents.title);
                        author1.setText(todayContents.author);
                        todayContent1.setText(todayContents.content);
                        TodayContents finalTodayContents = todayContents;
                        readButton1.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalTodayContents.url));
                            startActivity(intent);
                        });

                        contents1Loaded.set(true);
                        if(contents2Loaded.get()) {
                            loadDone = true;
                        }
            });

            backgroundtask = Observable.fromCallable(() -> {
                        TodayContents todayContents = new TodayContents();
                        todayContents.getColumn(1);

                        return todayContents;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((todayContents) -> {
                        todayTitle2.setText(todayContents.title);
                        author2.setText(todayContents.author);
                        todayContent2.setText(todayContents.content);
                        TodayContents finalTodayContents = todayContents;
                        readButton2.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalTodayContents.url));
                            startActivity(intent);
                        });

                        contents2Loaded.set(true);
                        if(contents1Loaded.get()) {
                            loadDone = true;
                        }
                    });
        } else {
            todayTitle1.setText("오늘의 문해력");
            todayTitle2.setText("오늘의 문해력");
            author1.setText("인터넷 연결이 필요합니다.");
            author2.setText("인터넷 연결이 필요합니다.");
            todayContent1.setText("인터넷 연결이 필요합니다.");
            todayContent2.setText("인터넷 연결이 필요합니다.");
            readButton1.setOnClickListener(v -> {});
            readButton2.setOnClickListener(v -> {});
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setProgressBar();
    }
}