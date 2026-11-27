package com.unrevr.munhaeryeok;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.unrevr.munhaeryeok.Alarm.AlarmController;
import com.unrevr.munhaeryeok.Alarm.AlarmData;
import com.unrevr.munhaeryeok.Alarm.AlarmListActivity;
import com.unrevr.munhaeryeok.Alarm.WrongProblemsListActivity;

import java.time.LocalDate;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends InsetActivity {
    private final CompositeDisposable columnRequests = new CompositeDisposable();
    private final TodayContents[] columns = new TodayContents[2];
    private LocalDate columnsDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        checkFirstRun();
        updateAppIfAvailable();

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
            Intent intent = new Intent(getApplicationContext(), WrongProblemsListActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.restudyText).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), WrongProblemsListActivity.class);
            startActivity(intent);
        });
    }

    void checkFirstRun() {
        DataController dataController = new DataController(getApplicationContext(), "isFirst");
        boolean first = dataController.getInt("isFirst", 0) == 0;

        if(first) {
            dataController.putInt("isFirst", 1);

            UserInfo userInfo = UserInfo.getInstance(getApplicationContext());
            userInfo.init();
        }
    }

    void updateAppIfAvailable() {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(this, appUpdateInfo -> {
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
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    void setProgressBar() {
        UserInfo userInfo = UserInfo.getInstance(getApplicationContext());
        userInfo.getUserInfo();
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView progressText = findViewById(R.id.progressText);

        progressBar.setProgress(userInfo.score);
        progressText.setText(userInfo.score + " / 100");
        if (isNetworkAvailable()) ScoreSharing.uploadIfDue(this, userInfo.id.trim(), userInfo.score);
    }

    void setNearestAlarm() {
        AlarmController alarms = new AlarmController(this);
        alarms.refreshAlarms();
        AlarmData next = alarms.nearest();
        TextView name = findViewById(R.id.problemTitle);
        TextView time = findViewById(R.id.problemTextView);
        TextView ampm = findViewById(R.id.amfmTextView);
        TextView days = findViewById(R.id.answerTextView);
        CheckBox favorite = findViewById(R.id.favoriteCheckBox);
        if (next == null) {
            name.setText(alarms.hasAlarms() && !alarms.canRing() ? "알람 권한이 필요합니다." : "울릴 알람이 없습니다.");
            time.setText("00:00");
            ampm.setText("");
            days.setText("");
            favorite.setVisibility(View.INVISIBLE);
            return;
        }
        name.setText(next.name);
        time.setText(next.timeLabel());
        ampm.setText(next.h >= 12 ? "PM" : "AM");
        days.setText(next.dayLabel());
        favorite.setVisibility(View.VISIBLE);
        favorite.setChecked(next.favorite);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalDate today = LocalDate.now();
        if (!today.equals(columnsDate)) {
            columnsDate = today;
            columns[0] = null;
            columns[1] = null;
        }
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == null) loadColumn(i);
        }
    }

    @Override
    protected void onStop() {
        columnRequests.clear();
        super.onStop();
    }

    private void loadColumn(int index) {
        TextView title = findViewById(index == 0 ? R.id.todayTitle1 : R.id.todayTitle2);
        TextView author = findViewById(index == 0 ? R.id.author1 : R.id.author2);
        TextView content = findViewById(index == 0 ? R.id.todayContent1 : R.id.todayContent2);
        Button button = findViewById(index == 0 ? R.id.readButton1 : R.id.readButton2);
        title.setText(R.string.today_column);
        author.setText("");
        button.setText(R.string.retry_column);
        button.setOnClickListener(v -> loadColumn(index));

        if (!isNetworkAvailable()) {
            content.setText(R.string.column_offline);
            button.setEnabled(true);
            return;
        }

        content.setText(R.string.column_loading);
        button.setEnabled(false);
        columnRequests.add(TodayContents.fetch(index)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(column -> {
                    columns[index] = column;
                    title.setText(column.title);
                    author.setText(column.author);
                    content.setText(column.content);
                    button.setEnabled(true);
                    button.setText(R.string.read_column);
                    button.setOnClickListener(v -> openColumn(column));
                }, error -> {
                    content.setText(R.string.column_unavailable);
                    button.setEnabled(true);
                }));
    }

    private void openColumn(TodayContents column) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(column.url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume");
        setProgressBar();
        setNearestAlarm();
    }
}
