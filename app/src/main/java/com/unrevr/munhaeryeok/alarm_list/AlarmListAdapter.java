package com.unrevr.munhaeryeok.alarm_list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unrevr.munhaeryeok.AlarmData;
import com.unrevr.munhaeryeok.R;

import java.util.ArrayList;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {
    private ArrayList<AlarmData> alarmItems;

    @NonNull
    @Override
    public AlarmListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmListAdapter.ViewHolder holder, int position) {
        holder.onBind(alarmItems.get(position));
    }

    @Override
    public int getItemCount() {
        return alarmItems.size();
    }

    public void setAlarmItems(ArrayList<AlarmData> alarmItems) {
        this.alarmItems = alarmItems;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView timeTextView;
        TextView amfmTextView;
        TextView dayTextView;
        CheckBox favoriteCheckBox;
        public ViewHolder(@NonNull View parent) {
            super(parent);

            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            amfmTextView = itemView.findViewById(R.id.amfmTextView);
            favoriteCheckBox = itemView.findViewById(R.id.favoriteCheckBox);
            dayTextView = itemView.findViewById(R.id.dayTextView);
        }

        void onBind(AlarmData alarmData) {
            nameTextView.setText(alarmData.name);
            int h = alarmData.h;
            if (h >= 12) {
                amfmTextView.setText("PM");
                if(h==12) {
                    timeTextView.setText("12:" + String.format("%02d",alarmData.m));
                } else {
                    timeTextView.setText(String.format("%02d",alarmData.h%12) + ";" + String.format("%02d",alarmData.m));
                }
            } else {
                amfmTextView.setText("AM");
                if(h==0) {
                    timeTextView.setText("12:" + String.format("%02d",alarmData.m));
                } else {
                    timeTextView.setText(String.format("%02d",alarmData.h) + ";" + String.format("%02d",alarmData.m));
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
            favoriteCheckBox.setChecked(alarmData.favorite);
        }
    }
}
