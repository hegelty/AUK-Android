package com.unrevr.munhaeryeok.Alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.R;

import java.util.List;

final class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {
    interface Actions {
        void edit(int id);
        void setFavorite(int id, boolean favorite);
    }

    private final List<AlarmData> alarms;
    private final Actions actions;

    AlarmListAdapter(List<AlarmData> alarms, Actions actions) {
        this.alarms = alarms;
        this.actions = actions;
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_list_item, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmData alarm = alarms.get(position);
        holder.name.setText(alarm.name);
        holder.time.setText(alarm.timeLabel());
        holder.ampm.setText(alarm.h >= 12 ? "PM" : "AM");
        holder.days.setText(alarm.dayLabel());
        holder.favorite.setChecked(alarm.favorite);
        holder.favorite.setOnClickListener(v -> actions.setFavorite(alarm.id, holder.favorite.isChecked()));
        holder.itemView.setOnClickListener(v -> actions.edit(alarm.id));
    }

    @Override public int getItemCount() { return alarms.size(); }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, time, ampm, days;
        final CheckBox favorite;

        ViewHolder(View row) {
            super(row);
            name = row.findViewById(R.id.problemTitle);
            time = row.findViewById(R.id.problemTextView);
            ampm = row.findViewById(R.id.amfmTextView);
            days = row.findViewById(R.id.answerTextView);
            favorite = row.findViewById(R.id.favoriteCheckBox);
        }
    }
}
