package com.unrevr.munhaeryeok.Alarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.unrevr.munhaeryeok.InsetActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.R;

public class AlarmListActivity extends InsetActivity implements AlarmListAdapter.Actions {
    private AlarmController alarms;
    private RecyclerView rows;
    private boolean favorites;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_list_layout);
        alarms = new AlarmController(this);
        rows = findViewById(R.id.wrongProblemsRecyclerView);
        rows.setLayoutManager(new LinearLayoutManager(this));
        AlarmAccess access = new AlarmAccess(this, alarms, this::refresh);
        favorites = savedInstanceState != null && savedInstanceState.getBoolean("favorites");
        findViewById(R.id.alarmAccess).setOnClickListener(v -> access.request());
        findViewById(R.id.backwardButton).setOnClickListener(v -> finish());
        findViewById(R.id.addButton).setOnClickListener(v -> edit(0));
        findViewById(R.id.favoriteButton).setOnClickListener(v -> { favorites = true; refresh(); });
        findViewById(R.id.allButton).setOnClickListener(v -> { favorites = false; refresh(); });
    }

    @Override protected void onResume() {
        super.onResume();
        alarms.refreshAlarms();
        refresh();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("favorites", favorites);
    }

    @Override public void edit(int id) {
        startActivity(new Intent(this, AlarmSettingActivity.class).putExtra("id", id));
    }

    @Override public void setFavorite(int id, boolean favorite) {
        alarms.setFavorite(id, favorite);
        refresh();
    }

    private void refresh() {
        findViewById(R.id.favoriteButton).setBackgroundResource(favorites ? R.drawable.round_corner_blue : R.drawable.round_corner_black);
        findViewById(R.id.allButton).setBackgroundResource(favorites ? R.drawable.round_corner_black : R.drawable.round_corner_blue);
        findViewById(R.id.alarmAccess).setVisibility(alarms.hasAlarms() && !alarms.canRing() ? View.VISIBLE : View.GONE);
        rows.setAdapter(new AlarmListAdapter(alarms.list(favorites), this));
    }
}
