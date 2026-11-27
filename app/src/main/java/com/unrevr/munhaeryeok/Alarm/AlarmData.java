package com.unrevr.munhaeryeok.Alarm;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Locale;

public final class AlarmData {
    public final int id, h, m;
    final int days, problemType;
    final boolean sound, vibration;
    public final String name;
    public final boolean favorite;

    AlarmData(int id, int h, int m, int days, boolean sound, boolean vibration,
              String name, int problemType, boolean favorite) {
        if (id < 0 || h < 0 || h > 23 || m < 0 || m > 59 || days < 1 || days > 127
                || problemType < 1 || problemType > 3) throw new IllegalArgumentException("잘못된 알람");
        this.id = id;
        this.h = h;
        this.m = m;
        this.days = days;
        this.sound = sound;
        this.vibration = vibration;
        this.name = name;
        this.problemType = problemType;
        this.favorite = favorite;
    }

    boolean repeatsOn(int sundayBasedDay) {
        return (days & (1 << sundayBasedDay)) != 0;
    }

    long nextAfter(ZonedDateTime now) {
        for (int offset = 0; offset <= 7; offset++) {
            LocalDate day = now.toLocalDate().plusDays(offset);
            if (!repeatsOn(day.getDayOfWeek().getValue() % 7)) continue;
            ZonedDateTime candidate = day.atTime(LocalTime.of(h, m)).atZone(now.getZone());
            if (candidate.isAfter(now)) return candidate.toInstant().toEpochMilli();
        }
        throw new IllegalStateException("반복 요일이 없습니다.");
    }

    public String dayLabel() {
        String[] labels = {"일", "월", "화", "수", "목", "금", "토"};
        ArrayList<String> selected = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) if (repeatsOn(i)) selected.add(labels[i]);
        return String.join(", ", selected);
    }

    public String timeLabel() {
        return String.format(Locale.KOREA, "%02d:%02d", h % 12 == 0 ? 12 : h % 12, m);
    }
}
