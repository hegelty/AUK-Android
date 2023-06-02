package com.unrevr.munhaeryeok.Alarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.skydoves.expandablelayout.ExpandableLayout;
import com.unrevr.munhaeryeok.DataController;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;
import java.util.Collections;

public class AlarmSettingActivity extends AppCompatActivity {
    int id;
    DataController dataCon;

    ExpandableLayout dayExpandableLayout, soundExpandableLayout, problemExpandableLayout;
    CheckBox soundCheckBox, vibrationCheckBox, mcCheckBox, sfCheckBox, favoriteCheckBox;
    TextView soundTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting_layout);
        dataCon = new DataController(getApplicationContext(), "alarm_data");

        dayExpandableLayout = findViewById(R.id.dayExpandableLayout);
        soundExpandableLayout = findViewById(R.id.soundExpandableLayout);
        problemExpandableLayout = findViewById(R.id.problemExpandableLayout);
        soundCheckBox = soundExpandableLayout.secondLayout.findViewById(R.id.soundCheckBox);
        vibrationCheckBox = soundExpandableLayout.secondLayout.findViewById(R.id.vibrationCheckBox);
        soundTextView = soundExpandableLayout.parentLayout.findViewById(R.id.soundSetTextView);
        mcCheckBox = problemExpandableLayout.secondLayout.findViewById(R.id.mcCheckBox);
        sfCheckBox = problemExpandableLayout.secondLayout.findViewById(R.id.sfCheckBox);
        favoriteCheckBox = findViewById(R.id.favoriteCheckBox);

        this.id = getIntent().getIntExtra("id", 0);
        if(id!=0) {
            loadAlarm(id);
        }

        setButton();
        setExpandableLayout();
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
        confirmButton.setOnClickListener(v -> {
            EditText nameEditText = findViewById(R.id.nameEditText);
            String name = nameEditText.getText().toString();
            if(name.trim().length()==0) {
                new AlertDialog.Builder(this)
                        .setTitle("오류")
                        .setMessage("알람 이름을 입력해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        }).show();
            }
            TextView dayTextView = dayExpandableLayout.parentLayout.findViewById(R.id.daySetTextView);
            String day = dayTextView.getText().toString();
            if(day.trim().length()==0) {
                new AlertDialog.Builder(this)
                        .setTitle("오류")
                        .setMessage("요일을 선택해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        }).show();
                return;
            }

            TextView soundTextView = soundExpandableLayout.parentLayout.findViewById(R.id.soundSetTextView);
            String sound = soundTextView.getText().toString();
            if(sound.trim().length()==0) {
                new AlertDialog.Builder(this)
                        .setTitle("오류")
                        .setMessage("알람 소리를 선택해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        }).show();
                return;
            }

            TextView problemTextView = problemExpandableLayout.parentLayout.findViewById(R.id.problemSetTextView);
            String problem = problemTextView.getText().toString();
            if(problem.trim().length()==0) {
                new AlertDialog.Builder(this)
                        .setTitle("오류")
                        .setMessage("문제를 선택해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        }).show();
                return;
            }

            if (setAlarm()) {
                finish();
            }
            else new AlertDialog.Builder(this)
                    .setTitle("오류")
                    .setMessage("알람 설정에 실패하였습니다.")
                    .setPositiveButton("확인", (dialog, which) -> {
                    }).show();
        });

        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("알람 삭제")
                    .setMessage("알람을 삭제하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        if(deleteAlarm(id)) {
                            finish();
                        } else {
                            new AlertDialog.Builder(this)
                                    .setTitle("오류")
                                    .setMessage("알람 삭제에 실패하였습니다.")
                                    .setPositiveButton("확인", (dialog1, which1) -> {
                                    }).show();
                        }
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                    })
                    .show();
        });

        Button backwardButton = findViewById(R.id.backwardButton);
        backwardButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("알람 설정 취소")
                    .setMessage("알람 설정을 취소하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        finish();
                    })
                    .setNegativeButton("취소", (dialog, which) -> {
                    })
                    .show();
        });
    }

    // ExpandableLayout
    void setExpandableLayout() {
        // 선택창 열고닫기
        dayExpandableLayout.setOnClickListener(v -> {
            if(dayExpandableLayout.isExpanded()) {
                CheckBox[] dayCheckBox = getCheckBoxes();

                String days = "";
                String[] dayString = {"일", "월", "화", "수", "목", "금", "토"};
                for(int i=0; i<7; i++) {
                    if(dayCheckBox[i].isChecked()) {
                        if(days!="") days += ", " + dayString[i];
                        else days = dayString[i];
                    }
                }
                if(dayCheckBox[1].isChecked()&&dayCheckBox[2].isChecked()&&dayCheckBox[3].isChecked()&&dayCheckBox[4].isChecked()&&dayCheckBox[5].isChecked()&&!dayCheckBox[6].isChecked()&&!dayCheckBox[0].isChecked()) {
                    days = "주중";
                    dayExpandableLayout.toggleLayout();
                }
                else if(dayCheckBox[0].isChecked()&&dayCheckBox[6].isChecked()&&dayCheckBox[1].isChecked()&&dayCheckBox[2].isChecked()&&dayCheckBox[3].isChecked()&&dayCheckBox[4].isChecked()&&dayCheckBox[5].isChecked()) {
                    days = "매일";
                    dayExpandableLayout.toggleLayout();
                }
                else if(dayCheckBox[0].isChecked()|dayCheckBox[6].isChecked()|dayCheckBox[1].isChecked()|dayCheckBox[2].isChecked()|dayCheckBox[3].isChecked()|dayCheckBox[4].isChecked()|dayCheckBox[5].isChecked()) {
                    dayExpandableLayout.toggleLayout();
                }

                TextView dayTextView = dayExpandableLayout.parentLayout.findViewById(R.id.daySetTextView);
                dayTextView.setText(days);
            }
            else {
                dayExpandableLayout.toggleLayout();
            }
        });

        soundExpandableLayout.setOnClickListener(v -> {
            if(soundExpandableLayout.isExpanded()) {
                soundExpandableLayout.toggleLayout();

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
            }
            else {
                soundExpandableLayout.toggleLayout();
            }
        });

        problemExpandableLayout.setOnClickListener(v -> {
            if(problemExpandableLayout.isExpanded()) {
                TextView problemTextView = problemExpandableLayout.parentLayout.findViewById(R.id.problemSetTextView);

                if (mcCheckBox.isChecked()) {
                    if (sfCheckBox.isChecked()) {
                        problemTextView.setText("객관식 + 주관식");
                        problemExpandableLayout.toggleLayout();
                    } else {
                        problemTextView.setText("객관식");
                        problemExpandableLayout.toggleLayout();
                    }
                } else {
                    if (sfCheckBox.isChecked()) {
                        problemTextView.setText("주관식");
                        problemExpandableLayout.toggleLayout();
                    } else {
                        problemTextView.setText("");
                    }
                }
            }
            else {
                problemExpandableLayout.toggleLayout();
            }
        });

        // 체크박스
        CheckBox[] checkBoxes = getCheckBoxes();
        ArrayList<CheckBox> checkBoxesArrayList = new ArrayList<>();
        Collections.addAll(checkBoxesArrayList, checkBoxes);
        checkBoxesArrayList.add(soundExpandableLayout.secondLayout.findViewById(R.id.vibrationCheckBox));
        checkBoxesArrayList.add(problemExpandableLayout.secondLayout.findViewById(R.id.mcCheckBox));
        checkBoxesArrayList.add(problemExpandableLayout.secondLayout.findViewById(R.id.sfCheckBox));
        for(CheckBox c: checkBoxesArrayList) {
            c.setOnClickListener(v -> updateSelects());
        }
    }

    int createID() {
        int id = dataCon.getInt("last_id", 0);
        dataCon.putInt("last_id", id+1);
        return id + 1;
    }

    boolean setAlarm() {
        try {
            if(id==0) id = createID();
            else deleteAlarm(id);

            TimePicker timePicker = findViewById(R.id.timePicker);
            int h = timePicker.getHour();
            int m = timePicker.getMinute();

            CheckBox[] dayCheckBox = getCheckBoxes();
            
            int problem_type = 0; // 1: 객관, 2: 주관, 3: 전부
            if(mcCheckBox.isChecked()) problem_type += 1;
            if(sfCheckBox.isChecked()) problem_type += 2;

            EditText nameEditText = findViewById(R.id.nameEditText);
            String name = nameEditText.getText().toString();

            boolean favorite = favoriteCheckBox.isChecked();

            AlarmController alarmController = new AlarmController(getApplicationContext());

            int[] days = new int[7];

            for(int i = 0; i < 7; i++) {
                if(dayCheckBox[i].isChecked()) {
                    int id = alarmController.setAlarm(i+1, h, m, 0, 0,
                            soundCheckBox.isChecked(), vibrationCheckBox.isChecked(), name, problem_type, favorite);
                    days[i] = id;
                }
            }

            AlarmData alarmData = new AlarmData(id, h, m, days, soundCheckBox.isChecked(), vibrationCheckBox.isChecked(), name, problem_type, favorite);
            saveAlarm(alarmData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    boolean deleteAlarm(int id) {
        try {
            if(id!=0) {
                AlarmData alarmData = getAlarmData(id);

                String original = dataCon.getString("alarms_list", "").trim();
                String[] list = original.split("=");
                String new_list = "";
                for(String s : list) {
                    if(Integer.parseInt(s.split("\\|")[0]) == id) {
                        Log.d("deleteAlarm", "delete " + s);
                        continue;
                    }
                    new_list += s + "=";
                }
                dataCon.putString("alarms_list", new_list);

                AlarmController alarmController = new AlarmController(getApplicationContext());
                for(int i=0;i<7;i++) {
                    if(alarmData.alarm_ids[i]!=0) {
                        Log.d("deleteAlarm", "delete(setting) " + alarmData.alarm_ids[i]);
                        alarmController.deleteAlarm(alarmData.alarm_ids[i]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void saveAlarm(AlarmData alarmData) {
        String original = dataCon.getString("alarms_list", "").trim();
        Log.d("saveAlarm", "data: " + alarmData.toString());
        dataCon.putString("alarms_list", original + alarmData.toString() + "=");
    }

    AlarmData getAlarmData(int id) {
        String original = dataCon.getString("alarms_list", "").trim();
        String[] list = original.split("=");
        for(String s : list) {
            if(Integer.parseInt(s.split("\\|")[0]) == id) {
                return new AlarmData(s);
            }
        }
        return null;
    }

    void loadAlarm(int id) {
        AlarmData alarmData = getAlarmData(id);

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setHour(alarmData.h);
        timePicker.setMinute(alarmData.m);

        CheckBox[] dayCheckBox = getCheckBoxes();

        for (int i=0;i<7;i++) {
            if(alarmData.alarm_ids[i]!=0) {
                dayCheckBox[i].setChecked(true);
            }
        }

        soundCheckBox.setChecked(alarmData.sound);
        vibrationCheckBox.setChecked(alarmData.vibration);

        if(alarmData.problem_type == 1) {
            mcCheckBox.setChecked(true);
        } else if(alarmData.problem_type == 2) {
            sfCheckBox.setChecked(true);
        } else if(alarmData.problem_type == 3) {
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
                if(days!="") days += ", " + dayString[i];
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("알람 수정")
                .setMessage("수정한 내용을 저장하지 않고 나가시겠습니까?")
                .setPositiveButton("나가기", (dialog, which) -> finish())
                .setNeutralButton("취소", null)
                .setNegativeButton("저장 후 나가기", (dialog, which) -> {
                    EditText nameEditText = findViewById(R.id.nameEditText);
                    String name = nameEditText.getText().toString();
                    if(name.trim().length()==0) {
                        new AlertDialog.Builder(this)
                                .setTitle("오류")
                                .setMessage("알람 이름을 입력해주세요.")
                                .setPositiveButton("확인", (dialog2, which2) -> {
                                }).show();
                    }
                    TextView dayTextView = dayExpandableLayout.parentLayout.findViewById(R.id.daySetTextView);
                    String day = dayTextView.getText().toString();
                    if(day.trim().length()==0) {
                        new AlertDialog.Builder(this)
                                .setTitle("오류")
                                .setMessage("요일을 선택해주세요.")
                                .setPositiveButton("확인", (dialog2, which2) -> {
                                }).show();
                        return;
                    }

                    TextView soundTextView = soundExpandableLayout.parentLayout.findViewById(R.id.soundSetTextView);
                    String sound = soundTextView.getText().toString();
                    if(sound.trim().length()==0) {
                        new AlertDialog.Builder(this)
                                .setTitle("오류")
                                .setMessage("알람 소리를 선택해주세요.")
                                .setPositiveButton("확인", (dialog2, which2) -> {
                                }).show();
                        return;
                    }

                    TextView problemTextView = problemExpandableLayout.parentLayout.findViewById(R.id.problemSetTextView);
                    String problem = problemTextView.getText().toString();
                    if(problem.trim().length()==0) {
                        new AlertDialog.Builder(this)
                                .setTitle("오류")
                                .setMessage("문제를 선택해주세요.")
                                .setPositiveButton("확인", (dialog2, which2) -> {
                                }).show();
                        return;
                    }
                    setAlarm();
                    finish();
                })
                .show();
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