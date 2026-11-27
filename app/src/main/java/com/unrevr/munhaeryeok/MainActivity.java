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
        AlarmController alarmController = new AlarmController(getApplicationContext());
        int nearestAlarmId = alarmController.getNearestAlarmId();
        Log.d("MainActivity", "nearestAlarmId: " + nearestAlarmId);
        if(nearestAlarmId == 0) {
            TextView nameTextView = findViewById(R.id.problemTitle);
            nameTextView.setText("울릴 알람이 없습니다.");
            TextView timeTextView = findViewById(R.id.problemTextView);
            timeTextView.setText("00:00");
            TextView amfmTextView = findViewById(R.id.amfmTextView);
            amfmTextView.setText("");
            TextView dayTextView = findViewById(R.id.answerTextView);
            dayTextView.setText("");
            CheckBox favoriteCheckBox = findViewById(R.id.favoriteCheckBox);
            favoriteCheckBox.setVisibility(View.INVISIBLE);
            return;
        }
        DataController dataCon = new DataController(getApplicationContext(),"alarm_data");
        String alarms_list = dataCon.getString("alarms_list", "").trim();
        String[] list = alarms_list.split("=");
        for (String s : list) {
            Log.d("MainActivity", s);
            if(s!=null) {
                AlarmData alarmData = new AlarmData(s);
                Log.d("MainActivity", alarmData.toString());
                for(int id: alarmData.alarm_ids) {
                    if(id == nearestAlarmId) {
                        TextView nameTextView = findViewById(R.id.problemTitle);
                        TextView timeTextView = findViewById(R.id.problemTextView);
                        TextView amfmTextView = findViewById(R.id.amfmTextView);
                        TextView dayTextView = findViewById(R.id.answerTextView);
                        CheckBox favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

                        nameTextView.setText(alarmData.name);
                        int h = alarmData.h;
                        if (h >= 12) {
                            amfmTextView.setText("PM");
                            if(h==12) {
                                timeTextView.setText("12:" + String.format("%02d",alarmData.m));
                            } else {
                                timeTextView.setText(String.format("%02d",alarmData.h%12) + ": " + String.format("%02d",alarmData.m));
                            }
                        } else {
                            amfmTextView.setText("AM");
                            if(h==0) {
                                timeTextView.setText("12:" + String.format("%02d",alarmData.m));
                            } else {
                                timeTextView.setText(String.format("%02d",alarmData.h) + ":" + String.format("%02d",alarmData.m));
                            }
                        }
                        String days = "";
                        String[] day = {"일", "월", "화", "수", "목", "금", "토"};
                        for(int i=0;i<7;i++) {
                            if(alarmData.alarm_ids[i]!=0) {
                                if(days=="") days = day[i];
                                else days = days + ", " + day[i];
                            }
                        }
                        dayTextView.setText(days);
                        favoriteCheckBox.setVisibility(View.VISIBLE);
                        favoriteCheckBox.setChecked(alarmData.favorite);
                        return;
                    }
                }
            }
        }
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
