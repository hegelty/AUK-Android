package com.unrevr.munhaeryeok.Alarm;

import android.app.AlertDialog;
import android.content.Context;
import androidx.activity.OnBackPressedCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.skydoves.expandablelayout.ExpandableLayout;

import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;
import java.util.Collections;

public class AlarmSettingActivity extends AppCompatActivity {
    int id;
    AlarmController alarms;
    AlarmAccess access;

    ExpandableLayout dayExpandableLayout, soundExpandableLayout, problemExpandableLayout;
    CheckBox soundCheckBox, vibrationCheckBox, mcCheckBox, sfCheckBox, favoriteCheckBox;
    TextView soundTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting_layout);
        alarms = new AlarmController(this);
        access = new AlarmAccess(this, alarms, this::finish);

        dayExpandableLayout = findViewById(R.id.dayExpandableLayout);
        soundExpandableLayout = findViewById(R.id.soundExpandableLayout);
        problemExpandableLayout = findViewById(R.id.problemExpandableLayout);
        soundCheckBox = soundExpandableLayout.secondLayout.findViewById(R.id.soundCheckBox);
        vibrationCheckBox = soundExpandableLayout.secondLayout.findViewById(R.id.vibrationCheckBox);
        soundTextView = soundExpandableLayout.parentLayout.findViewById(R.id.soundSetTextView);
        mcCheckBox = problemExpandableLayout.secondLayout.findViewById(R.id.mcCheckBox);
        sfCheckBox = problemExpandableLayout.secondLayout.findViewById(R.id.sfCheckBox);
        favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

        this.id = savedInstanceState == null ? getIntent().getIntExtra("id", 0) : savedInstanceState.getInt("id");
        if(id!=0) {
            loadAlarm(id);
        }

        setButton();
        setExpandableLayout();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { confirmExit(); }
        });
    }

    // EditText에서 포커스 풀리면 키보드 숨기기
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) {
                ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
                EditText editText = findViewById(R.id.nameEditText);
                editText.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // 버튼 동작 설정
    void setButton() {
        Button confirmButton = findViewById(R.id.ConfirmButton);
        confirmButton.setOnClickListener(v -> save());

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(id == 0 ? View.GONE : View.VISIBLE);
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("알람 삭제")
                    .setMessage("알람을 삭제하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        alarms.delete(id);
                        finish();
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                    })
                    .show();
        });

        ImageButton backwardButton = findViewById(R.id.backwardButton);
        backwardButton.setOnClickListener(v -> confirmExit());
    }

    void setExpandableLayout() {
        for (ExpandableLayout group : new ExpandableLayout[]{dayExpandableLayout, soundExpandableLayout, problemExpandableLayout})
            group.setOnClickListener(v -> group.toggleLayout());
        ArrayList<CheckBox> choices = new ArrayList<>();
        Collections.addAll(choices, getCheckBoxes());
        Collections.addAll(choices, soundCheckBox, vibrationCheckBox, mcCheckBox, sfCheckBox);
        for (CheckBox choice : choices) choice.setOnCheckedChangeListener((box, checked) -> updateSelects());
        updateSelects();
    }

    void save() {
        String name = ((EditText) findViewById(R.id.nameEditText)).getText().toString().trim();
        CheckBox[] boxes = getCheckBoxes();
        int days = 0;
        for (int i = 0; i < boxes.length; i++) if (boxes[i].isChecked()) days |= 1 << i;
        int problem = (mcCheckBox.isChecked() ? 1 : 0) + (sfCheckBox.isChecked() ? 2 : 0);
        String message = null;
        if (name.isEmpty()) message = "알람 이름을 입력해주세요.";
        else if (days == 0) message = "요일을 선택해주세요.";
        else if (problem == 0) message = "문제를 선택해주세요.";
        if (message == null) {
            TimePicker time = findViewById(R.id.timePicker);
            try {
                id = alarms.save(new AlarmData(id, time.getHour(), time.getMinute(), days,
                        soundCheckBox.isChecked(), vibrationCheckBox.isChecked(), name, problem,
                        favoriteCheckBox.isChecked()));
                access.request();
                return;
            } catch (RuntimeException e) {
                Log.e("AlarmSetting", "Cannot save alarm", e);
                message = "알람 설정을 저장할 수 없습니다.";
            }
        }
        new AlertDialog.Builder(this).setTitle("알람 설정").setMessage(message).setPositiveButton("확인", null).show();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("id", id);
    }

    void loadAlarm(int id) {
        AlarmData alarmData = alarms.get(id);
        if (alarmData == null) { finish(); return; }

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setHour(alarmData.h);
        timePicker.setMinute(alarmData.m);

        CheckBox[] dayCheckBox = getCheckBoxes();

        for (int i=0;i<7;i++) {
            if(alarmData.repeatsOn(i)) {
                dayCheckBox[i].setChecked(true);
            }
        }

        soundCheckBox.setChecked(alarmData.sound);
        vibrationCheckBox.setChecked(alarmData.vibration);

        if(alarmData.problemType == 1) {
            mcCheckBox.setChecked(true);
        } else if(alarmData.problemType == 2) {
            sfCheckBox.setChecked(true);
        } else if(alarmData.problemType == 3) {
            mcCheckBox.setChecked(true);
            sfCheckBox.setChecked(true);
        }

        EditText nameEditText = findViewById(R.id.nameEditText);
        nameEditText.setText(alarmData.name);

        CheckBox favoriteCheckBox = findViewById(R.id.favoriteCheckBox);
        favoriteCheckBox.setChecked(alarmData.favorite);

        updateSelects();
    }

    void updateSelects() {
        CheckBox[] dayCheckBox = getCheckBoxes();

        String days = "";
        String[] dayString = {"일", "월", "화", "수", "목", "금", "토"};
        for(int i=0; i<7; i++) {
            if(dayCheckBox[i].isChecked()) {
                if(!days.isEmpty()) days += ", " + dayString[i];
                else days = dayString[i];
            }
        }
        if(dayCheckBox[1].isChecked()&&dayCheckBox[2].isChecked()&&dayCheckBox[3].isChecked()&&dayCheckBox[4].isChecked()&&dayCheckBox[5].isChecked()&&!dayCheckBox[6].isChecked()&&!dayCheckBox[0].isChecked()) {
            days = "주중";
        }
        else if(dayCheckBox[0].isChecked()&&dayCheckBox[6].isChecked()&&dayCheckBox[1].isChecked()&&dayCheckBox[2].isChecked()&&dayCheckBox[3].isChecked()&&dayCheckBox[4].isChecked()&&dayCheckBox[5].isChecked()) {
            days = "매일";
        }

        TextView dayTextView = dayExpandableLayout.parentLayout.findViewById(R.id.daySetTextView);
        dayTextView.setText(days);

        // 소리, 진동
        if(soundCheckBox.isChecked()) {
            if(vibrationCheckBox.isChecked()) {
                soundTextView.setText("소리 + 진동");
            }
            else {
                soundTextView.setText("소리");
            }
        }
        else {
            if(vibrationCheckBox.isChecked()) {
                soundTextView.setText("진동");
            }
            else {
                soundTextView.setText("무음");
            }
        }

        // 문제
        TextView problemTextView = problemExpandableLayout.parentLayout.findViewById(R.id.problemSetTextView);

        if (mcCheckBox.isChecked()) {
            if (sfCheckBox.isChecked()) {
                problemTextView.setText("객관식 + 주관식");
            } else {
                problemTextView.setText("객관식");
            }
        } else {
            if (sfCheckBox.isChecked()) {
                problemTextView.setText("주관식");
            } else {
                problemTextView.setText("");
            }
        }
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("알람 수정").setMessage("수정한 내용을 저장하지 않고 나가시겠습니까?")
                .setPositiveButton("나가기", (dialog, which) -> finish())
                .setNeutralButton("취소", null)
                .setNegativeButton("저장 후 나가기", (dialog, which) -> save()).show();
    }

    CheckBox[] getCheckBoxes() {
        ExpandableLayout dayExpandableLayout = findViewById(R.id.dayExpandableLayout);
        CheckBox[] dayCheckBox = new CheckBox[7];
        dayCheckBox[1] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxMonday));
        dayCheckBox[2] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxTuesday));
        dayCheckBox[3] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxWednesday));
        dayCheckBox[4] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxThursday));
        dayCheckBox[5] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxFriday));
        dayCheckBox[6] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxSaturday));
        dayCheckBox[0] = (dayExpandableLayout.secondLayout.findViewById(R.id.checkBoxSunday));
        return dayCheckBox;
    }
}