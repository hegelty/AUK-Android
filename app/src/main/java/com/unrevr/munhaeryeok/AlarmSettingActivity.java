package com.unrevr.munhaeryeok;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.unrevr.munhaeryeok.Alarm.AlarmController;

import java.util.ArrayList;
import java.util.List;

public class AlarmSettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting_layout);
        // 알람 설정 화면

        Button confirmButton = findViewById(R.id.ConfirmButton);
        confirmButton.setOnClickListener(v -> {
            TimePicker timePicker = findViewById(R.id.timePicker);
            int h = timePicker.getHour();
            int m = timePicker.getMinute();

            CheckBox[] dayCheckBox = new CheckBox[7];
            dayCheckBox[1] = (findViewById(R.id.checkBoxMonday));
            dayCheckBox[2] = (findViewById(R.id.checkBoxTuesday));
            dayCheckBox[3] = (findViewById(R.id.checkBoxWednesday));
            dayCheckBox[4] = (findViewById(R.id.checkBoxThursday));
            dayCheckBox[5] = (findViewById(R.id.checkBoxFriday));
            dayCheckBox[6] = (findViewById(R.id.checkBoxSaturday));
            dayCheckBox[0] = (findViewById(R.id.checkBoxSunday));

            Switch soundSwitch = findViewById(R.id.soundSwitch);
            Switch vibrationSwitch = findViewById(R.id.vibrationSwitch);

            EditText nameEditText = findViewById(R.id.nameEditText);
            String name = nameEditText.getText().toString();

            CheckBox favoriteCheckBox = findViewById(R.id.favoriteCheckBox);
            boolean favorite = favoriteCheckBox.isChecked();

            AlarmController alarmController = new AlarmController(getApplicationContext());
            for(int i = 0; i < 7; i++) {
                if(dayCheckBox[i].isChecked()) {
                    alarmController.setAlarm(i+1, h, m, 0, 0,
                            soundSwitch.isActivated(), vibrationSwitch.isActivated(), name, problem_type, favorite);
                }
            }
        });

        Button cancelButton = findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(v -> {
            finish();
        });
    }
}
